package models

import play.api.libs.json.Json
import reactivemongo.api.bson.compat.fromDocument
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import java.time.{Instant, LocalTime, ZoneId}
import scala.util.Try

case class FlightDetails(id : String,
                         airline : String,
                         flightNo : Int,
                         tailNo : String,
                         origin : String,
                         destination : String,
                         departureTime : LocalTime,
                         arrivalTime : LocalTime,
                         airTime : String,
                         distance : String,
                         totalSeats : Int = 180,
                         pnr : String,
                         price : Double
                        )

object FlightDetails {
  implicit val flightFormat = Json.format[FlightDetails]
  implicit object FlightsBSONReader extends BSONDocumentReader[FlightDetails] {
    override def readDocument(bson: BSONDocument): Try[FlightDetails] = {
      Try(FlightDetails(
        bson.getAs[String]("_id").get,
        bson.getAs[String]("airline").get,
        bson.getAs[Int]("flightNo").get,
        bson.getAs[String]("tailNo").get,
        bson.getAs[String]("origin").get,
        bson.getAs[String]("destination").get,
        bson.getAs[Long]("departureTime").map(dt => LocalTime.ofInstant(Instant.ofEpochMilli(dt), ZoneId.systemDefault())).get,
        bson.getAs[Long]("arrivalTime").map(dt => LocalTime.ofInstant(Instant.ofEpochMilli(dt), ZoneId.systemDefault())).get,
        bson.getAs[String]("airTime").get,
        bson.getAs[String]("distance").get,
        bson.getAs[Int]("totalSeats").get,
        bson.getAs[String]("pnr").get,
        bson.getAsTry[Double]("price").get))
    }
  }


  implicit object FlightsBSONWriter extends BSONDocumentWriter[FlightDetails] {
     def writeTry(fd: FlightDetails): Try[BSONDocument] = {
      Try(BSONDocument("_id" -> fd.id,
        "airline" -> fd.airline,
        "flightNo" -> fd.flightNo,
        "tailNo" -> fd.tailNo,
        "origin" -> fd.origin,
        "destination" -> fd.destination,
        "departureTime" -> fd.departureTime,
        "arrivalTime" -> fd.arrivalTime,
        "airTime" -> fd.airTime,
        "distance" -> fd.distance,
        "totalSeats" -> fd.totalSeats,
        "pnr" -> fd.pnr,
        "price" -> fd.price))
    }

  }
}

