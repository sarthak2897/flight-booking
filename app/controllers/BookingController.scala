package controllers

import actors.BookingActor
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import models.{BookingDetails, Ticket}
import modules.TicketBookingApplication
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

@Singleton
class BookingController @Inject() (implicit val ec : ExecutionContext,
                                   implicit val mat : Materializer,
                                   val ticketBookingApplication: TicketBookingApplication,
                                    val controllerComponents: ControllerComponents) extends BaseController{

  final val logger : Logger = Logger(this.getClass)
  implicit val timeout: Timeout = 30.seconds

  def bookFlight() =
    Action.async{ implicit request =>
    logger.info("Booking your flight.")
      val payload  = request.body.asJson.get.as[Ticket]
      (ticketBookingApplication.bookingActor ? BookingActor.BookTicket(payload))
        .mapTo[BookingDetails].map(x => Ok(Json.toJson(x)))
  }

  def fetchBookedFlightsPerCustomer(customerId : Int) = Action.async{
    implicit request =>
      logger.info(s"Fetching booked flights for customer ${customerId}")
      (ticketBookingApplication.bookingActor ? BookingActor.FetchBookedTickets(customerId))
        .mapTo[List[BookingDetails]].map(x => Ok(Json.toJson(x)))
  }

  def cancelBookedTicketPerCustomer(customerId : Int, bookingId : String) = Action.async{
    implicit request =>
      logger.info(s"Attempting to cancel the flight for customer ${customerId} having booking ${bookingId}")
      (ticketBookingApplication.bookingActor ? BookingActor.CancelBookedTicket(customerId,bookingId))
        .mapTo[String].map(x => Ok(Json.toJson(x)))
  }


}
