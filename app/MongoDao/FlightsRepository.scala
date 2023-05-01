package MongoDao

import models.FlightDetails
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.commands.WriteResult

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FlightsRepository @Inject() (implicit val ec : ExecutionContext,
                                   reactiveMongoApi: ReactiveMongoApi){

  val logger : Logger = Logger(this.getClass)

  def collection : Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("flights"))

  def insertFlightRecords(flightDetails : FlightDetails): Future[WriteResult] = {
    collection.flatMap(_.insert.one(flightDetails))
  }

  def findAllFlightRecords(limit : Int) = {
    collection.flatMap(_.find(BSONDocument(),Option.empty[FlightDetails])
      .cursor[FlightDetails]().collect[List]())
  }

  def findFlightRecordsById(id : String) = {
    logger.info("Finding flight records")
    collection.flatMap(_.find
    //(BSONDocument("airline" -> airlineName, "flightNo" -> flightNo, "source" -> source, "destination" -> destination)
    (BSONDocument("_id" -> id)
      , Option.empty[FlightDetails])
      .cursor[FlightDetails]().collect[List]())
  }

  def totalRecords() = {
    collection.flatMap(x => x.count())
  }

  def updateTotalSeats(bsonId : String, flightDetails: FlightDetails) = {
    logger.info(s"Updating total seats after successful booking for the airline ${flightDetails.airline} and flight " +
      s"no ${flightDetails.flightNo}")
    collection.flatMap(_.update(ordered = false).one(BSONDocument("_id" -> bsonId),flightDetails))
  }

}
