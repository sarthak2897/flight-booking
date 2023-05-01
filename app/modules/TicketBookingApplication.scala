package modules

import MongoDao.{FlightsRepository, TicketBookingRepository}
import actors.BookingActor
import akka.actor.ActorSystem
import play.api.mvc.{BaseController, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TicketBookingApplication @Inject() (val ac : ActorSystem,
                                          val flightsRepository: FlightsRepository,
                                          val ticketBookingRepository: TicketBookingRepository,
                                          val controllerComponents: ControllerComponents) (implicit val ec: ExecutionContext) extends BaseController{

  val bookingActor = ac.actorOf(BookingActor.props(flightsRepository,ticketBookingRepository),"bookingActor")
}
