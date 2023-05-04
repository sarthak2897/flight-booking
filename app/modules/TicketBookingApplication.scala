package modules

import MongoDao.{FlightsRepository, PaymentsRepository, TicketBookingRepository}
import actors.BookingActor
import akka.actor.ActorSystem

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TicketBookingApplication @Inject() (val ac : ActorSystem,
                                          val flightsRepository: FlightsRepository,
                                          val ticketBookingRepository: TicketBookingRepository,
                                          val paymentsRepository: PaymentsRepository
                                          ) (implicit val ec: ExecutionContext) {

  val bookingActor = ac.actorOf(BookingActor.props(flightsRepository,ticketBookingRepository,paymentsRepository),"bookingActor")
}
