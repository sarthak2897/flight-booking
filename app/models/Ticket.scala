package models

import play.api.libs.json.Json
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}
import scala.util.Try

case class Ticket(bookingId : Option[String],
                   airline : String,
                  flightNo : Int,
                  source : String,
                  destination: String,
                  departureDate : LocalDate,
                  totalSeats : Int)

case class BookingDetails(customerId : Int = 1,
                          ticket : Ticket,
                          pnr : String,
                          bookingTime : LocalDateTime,
                          departureTime : LocalDateTime,
                          message : Option[String])

object Ticket {
  implicit val ticketFormat = Json.format[Ticket]

  implicit object TicketBSONReader extends BSONDocumentReader[Ticket] {
    override def readDocument(bson: BSONDocument): Try[Ticket] = {
      Try(Ticket(
        bson.getAsTry[String]("bookingId").toOption,
        bson.getAsTry[String]("airline").get,
        bson.getAsTry[Int]("flightNo").get,
        bson.getAsTry[String]("source").get,
        bson.getAsTry[String]("destination").get,
        bson.getAsTry[Long]("departureDate").map(dt => Instant.ofEpochMilli(dt).atZone(ZoneId.systemDefault).toLocalDate).get,
        bson.getAsTry[Int]("totalSeats").get
      ))
    }
  }

  implicit object TicketBSONWriter extends BSONDocumentWriter[Ticket] {
    override def writeTry(t: Ticket): Try[BSONDocument] = {
      Try(BSONDocument(
        "bookingId" -> t.bookingId.get,
        "airline" -> t.airline,
        "flightNo" -> t.flightNo,
        "source" -> t.source,
        "destination" -> t.destination,
        "departureDate" -> t.departureDate,
        "totalSeats" -> t.totalSeats
      ))
    }
  }
}

object BookingDetails {
  implicit val bookingFormat = Json.format[BookingDetails]

  implicit object BookingBSONReader extends BSONDocumentReader[BookingDetails] {
    override def readDocument(bson: BSONDocument): Try[BookingDetails] = {
      Try(BookingDetails(
        bson.getAsTry[Int]("customerId").get,
        bson.getAsTry[Ticket]("ticket").get,
        bson.getAsTry[String]("pnr").get,
        bson.getAsTry[Long]("bookingTime").map(x => LocalDateTime.ofInstant(Instant.ofEpochMilli(x),ZoneId.systemDefault())).get,
        bson.getAsTry[Long]("departureTime").map(dt => LocalDateTime.ofInstant(Instant.ofEpochMilli(dt), ZoneId.systemDefault())).get,
        bson.getAsTry[String]("message").toOption))
    }
  }


  implicit object BookingBSONWriter extends BSONDocumentWriter[BookingDetails] {
    override def writeTry(fd: BookingDetails): Try[BSONDocument] = {
      Try(BSONDocument(
        "customerId" -> fd.customerId,
        "ticket" -> fd.ticket,
        "pnr" -> fd.pnr,
        "bookingTime" -> fd.bookingTime,
        "departureTime" -> fd.departureTime,
        "message" -> fd.message.get
      ))
    }

  }

}


