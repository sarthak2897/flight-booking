package actors

import MongoDao.{FlightsRepository, PaymentsRepository, TicketBookingRepository}
import actors.BookingActor.{BookTicket, CancelBookedTicket, FetchBookedTickets}
import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import models.{BookingDetails, Ticket}
import play.api.Logger
import play.api.libs.json.Json
import utils.Utils.generateBookingId

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object BookingActor {

  def props(flightsRepository : FlightsRepository, ticketBookingRepository : TicketBookingRepository,
  paymentsRepository : PaymentsRepository)(implicit ec : ExecutionContext) =
    Props(new BookingActor(flightsRepository,ticketBookingRepository,paymentsRepository))
  case class BookTicket(ticket : Ticket)
  case class FetchBookedTickets(customerId : Int)
  case class CancelBookedTicket(customerId : Int,bookingId : String)

  //sealed trait EventResponse
  case class BookingCompleted(booking : BookingDetails)
  object BookingCompleted {
    implicit val eventResponseFormat = Json.format[BookingCompleted]
  }
}

class BookingActor @Inject() (flightsRepository : FlightsRepository, ticketBookingRepository
: TicketBookingRepository,paymentsRepository: PaymentsRepository)(implicit val ec : ExecutionContext) extends Actor {

   implicit val timeout: Timeout = 30.seconds
   val logger : Logger = Logger(this.getClass)

  def createTicketManager(bookingId : String) = {
    val id = f"book-$bookingId"
    context.actorOf(TicketManagementActor.props(flightsRepository,ticketBookingRepository,paymentsRepository), id)
  }

  override def receive: Receive = {
    case BookTicket(ticket) =>
      val bookingId = generateBookingId(ticket.airline,ticket.flightNo)
      val completeTicket = ticket.copy(bookingId = Some(bookingId))
      val ticketManager = createTicketManager(bookingId)
      val bookingDetails = (ticketManager ? TicketManagementActor.Buy(completeTicket)).mapTo[BookingDetails]
      bookingDetails pipeTo sender()

    case FetchBookedTickets(customerId) =>
      ticketBookingRepository.fetchBookedTicketsPerCustomer(customerId) pipeTo sender()

    case CancelBookedTicket(customerId,bookingId) =>
      val ticketManager = createTicketManager(bookingId+customerId)
      (ticketManager ? TicketManagementActor.CancelBooking(customerId, bookingId)).mapTo[String] pipeTo sender()
  }
}
