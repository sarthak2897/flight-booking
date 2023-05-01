package actors

import MongoDao.{FlightsRepository, TicketBookingRepository}
import akka.actor.{Actor, Props}
import akka.pattern.pipe
import models.{BookingDetails, Ticket}
import play.api.Logger
import utils.Utils

import java.time.LocalTime
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
        val fd = flightDetail.head
        val bookingMessage = BookingDetails(ticket = ticket, pnr = fd.pnr,bookingTime = LocalTime.now(), departureTime= fd.departureTime,message = None)
        val totalFlightSeats = fd.totalSeats
        if(totalFlightSeats < ticket.totalSeats)
          Future(bookingMessage.copy(message = Some(s"Seats for airline ${ticket.airline} ,flight no ${ticket
            .flightNo} departing from ${ticket.source} to ${ticket.destination}")))
        else{
          val successBookingMessage = bookingMessage.copy(message = Some(s"Successfully booked ticket."))
          for {
            _ <- flightsRepository.updateTotalSeats(fd.id,fd.copy(totalSeats = totalFlightSeats - ticket.totalSeats))
            _ <- ticketBookingRepository.bookFlightTickets(successBookingMessage)
          } yield successBookingMessage
        }
      })
      bookingDetails pipeTo sender()

    case CancelBooking(customerId,bookingId) =>
      //Compare if the current time is 2 hrs behind the departure time, then only the ticket can be cancelled
      val bookingDetails = ticketBookingRepository.fetchTicketsPerBookingId(bookingId).flatMap(x => {
        val bookingDetail = x.head
        if(bookingDetail.departureTime.until(LocalTime.now(), ChronoUnit.HOURS) >= 2) {
          val id = Utils.generateId(bookingDetail.ticket.airline+bookingDetail.ticket.flightNo.toString+
            bookingDetail.ticket.source+bookingDetail.ticket.destination)
          for {
            flightDetails <- flightsRepository.findFlightRecordsById(id)
            _ <- ticketBookingRepository.cancelBookedTicketPerCustomer(customerId,bookingId)
            _ <- flightsRepository.updateTotalSeats(id,flightDetails.head.copy(totalSeats = flightDetails.head.totalSeats + bookingDetail.ticket.totalSeats))
          } yield s"Flight booking for airline ${bookingDetail.ticket.airline} and flight no ${bookingDetail.ticket
            .flightNo} departing at ${bookingDetail.departureTime} from ${bookingDetail.ticket.source} having " +
            s"booking id ${bookingId} has been cancelled."
        }
        else Future(s"Flight having booking Id ${bookingId} cannot be cancelled less than two hours of departure.")
      })
      bookingDetails pipeTo sender()
  }
}
