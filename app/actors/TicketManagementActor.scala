package actors

import MongoDao.{FlightsRepository, TicketBookingRepository}
import akka.actor.{Actor, Props}
import akka.pattern.pipe
import models.{BookingDetails, FlightDetails, Ticket}
import play.api.Logger
import utils.Utils

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

object TicketManagementActor {
  def props(flightsRepository : FlightsRepository, ticketBookingRepository : TicketBookingRepository)
           (implicit ec : ExecutionContext) = Props(new TicketManagementActor(flightsRepository,ticketBookingRepository))
  case class Buy(ticket : Ticket)
  case class CancelBooking(customerId : Int,bookingId : String)
}

class TicketManagementActor @Inject()(val flightsRepository: FlightsRepository,val ticketBookingRepository: TicketBookingRepository)
                                     (implicit ec: ExecutionContext)  extends Actor{

  final val logger : Logger = Logger(this.getClass)
  import TicketManagementActor._
  override def receive: Receive = {
    case Buy(ticket) =>
      logger.debug(s"Booking ticket ${ticket}")
      val flightDetails = flightsRepository.findFlightRecordsById(Utils.generateId(ticket.airline+ticket.flightNo.toString+ticket.source+ticket.destination))
      val bookingDetails : Future[BookingDetails] = flightDetails.flatMap(flightDetail => {
        if(flightDetail.isEmpty)
          Future(BookingDetails(ticket = ticket,pnr = null,bookingTime = LocalDateTime.now(), departureTime = LocalDateTime.MIN, message = Some(Utils.FLIGHT_NOT_FOUND)))
        else bookTicket(flightDetail,ticket)
      })
      bookingDetails pipeTo sender()

    case CancelBooking(customerId,bookingId) =>
      val bookingDetails = ticketBookingRepository.fetchTicketsPerBookingId(bookingId).flatMap(ticket => {
        if(ticket.isEmpty)
          Future(Utils.bookingNotFound(bookingId,customerId))
        else cancelTicket(ticket,customerId,bookingId)
      })
      bookingDetails pipeTo sender()
  }

  def bookTicket(flightDetail : List[FlightDetails], ticket : Ticket) = {
    val fd = flightDetail.head
    val bookingMessage = BookingDetails(ticket = ticket, pnr = fd.pnr, bookingTime = LocalDateTime.now(),
      departureTime = fd.departureTime.atDate(ticket.departureDate), message = None)
    val totalFlightSeats = fd.totalSeats
    if (totalFlightSeats < ticket.totalSeats)
      Future(bookingMessage.copy(message = Some(Utils.seatsFullMessage(ticket))))
    else {
      val successBookingMessage = bookingMessage.copy(message = Some(s"Successfully booked ticket."))
      for {
        _ <- flightsRepository.updateTotalSeats(fd.id, fd.copy(totalSeats = totalFlightSeats - ticket.totalSeats))
        _ <- ticketBookingRepository.bookFlightTickets(successBookingMessage)
      } yield successBookingMessage
    }
  }

  def cancelTicket(ticket : Option[BookingDetails],customerId : Int, bookingId : String) = {
    val bookingDetail = ticket.head
    //Compare if the current time is 2 hrs behind the departure time, then only the ticket can be cancelled
    if (bookingDetail.departureTime.until(LocalDateTime.now(), ChronoUnit.HOURS) >= 2) {
      val id = Utils.generateId(bookingDetail.ticket.airline + bookingDetail.ticket.flightNo.toString +
        bookingDetail.ticket.source + bookingDetail.ticket.destination)
      for {
        flightDetails <- flightsRepository.findFlightRecordsById(id)
        _ <- ticketBookingRepository.cancelBookedTicketPerCustomer(customerId, bookingId)
        _ <- flightsRepository.updateTotalSeats(id, flightDetails.head.copy(totalSeats = flightDetails.head.totalSeats + bookingDetail.ticket.totalSeats))

      } yield Utils.ticketCancellationSuccess(bookingDetail,bookingId)
    }
    else Future(Utils.ticketCancellationFailure(bookingId))
  }
}
