package models

import play.api.libs.json.Json
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import scala.util.Try

case class PaymentRequest(name : String,
                   amount : Long,
                   email : String,
                   cardDigits : String,
                   bookingId : String)

case class PaymentResponse(customerId : Int,
                           bookingId : String,
                            //name : String,
                           amount : Long,
                           //email : String,
                           paymentId : String,

                           status : String)


object PaymentRequest {
  implicit val paymentRequestFormat = Json.format[PaymentRequest]
}

object PaymentResponse {
  implicit val paymentResponseFormat = Json.format[PaymentResponse]

  //Commenting below fields for now until front end is integrated for payments
  implicit object PaymentResponseBSONReader extends BSONDocumentReader[PaymentResponse] {
    override def readDocument(doc: BSONDocument): Try[PaymentResponse] = {
      Try(PaymentResponse(
        doc.getAsTry[Int]("customerId").get,
        doc.getAsTry[String]("bookingId").get,
        //doc.getAsTry[String]("name").get,
        doc.getAsTry[Long]("amount").get,
        //doc.getAsTry[String]("email").get,
        doc.getAsTry[String]("paymentId").get,
        doc.getAsTry[String]("status").get
      ))
    }
  }

  implicit object PaymentResponseBSONWriter extends BSONDocumentWriter[PaymentResponse] {
    override def writeTry(payment : PaymentResponse): Try[BSONDocument] = {
      Try(BSONDocument(
        "customerId" -> payment.customerId,
        "bookingId" -> payment.bookingId,
        //"name" -> payment.name,
        "amount" -> payment.amount,
        //"email" -> payment.email,
        "paymentId" -> payment.paymentId,
        "status" -> payment.status
      ))
    }
  }
}

