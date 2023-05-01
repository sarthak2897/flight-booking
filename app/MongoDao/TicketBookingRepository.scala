package MongoDao

import models.BookingDetails
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TicketBookingRepository @Inject() (implicit val ec : ExecutionContext,
                                   reactiveMongoApi: ReactiveMongoApi){

  val logger : Logger = Logger(this.getClass)

  def collection : Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("bookings"))

  def bookFlightTickets(bookingDetails : BookingDetails): Future[WriteResult] = {
    logger.info(s"Making entry for a successful booking for ticket ${bookingDetails.ticket.bookingId}")
    collection.flatMap(_.insert.one(bookingDetails))
  }

  def fetchBookedTicketsPerCustomer(customerId : Int) = {
    collection.flatMap(_.find(BSONDocument("customerId" -> customerId),Option.empty[BookingDetails])
      .cursor[BookingDetails]().collect[List]())
  }

  def cancelBookedTicketPerCustomer(customerId : Int,bookingId : String) = {
    collection.flatMap(_.delete().one(BSONDocument("customerId" -> customerId,"ticket.bookingId" -> bookingId)))
  }

  def fetchTicketsPerBookingId(bookingId : String) = {
    collection.flatMap(_.find(BSONDocument("ticket.bookingId" -> bookingId)).one[BookingDetails])
  }

//  def findAllFlightRecords(limit : Int) = {
//    collection.flatMap(_.find(BSONDocument(),Option.empty[FlightDetails])
//      .cursor[FlightDetails]().collect[List]())
//  }
//
//  def findFlightRecordsByAirline(airlineName : String) = {
//    collection.flatMap(_.find(BSONDocument("airline" -> airlineName), Option
//      .empty[FlightDetails]).cursor[FlightDetails]().collect[List]())
//  }
//
//  def totalRecords() = {
//    collection.flatMap(x => x.count())
//  }

}
