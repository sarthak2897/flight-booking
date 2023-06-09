package utils

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import models.{BookingDetails, Ticket}

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalTime

object Utils {

  lazy val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  def formatTime(time : String): LocalTime = {
    val stamps = (time.length match {
      case 3 => time.substring(0,1)+":"+time.substring(1)
      case 4 => time.substring(0,2)+":"+time.substring(2)
      case _ => "00:00" //TODO throw user exception
    }).split(":")

    val hours = stamps(0).toInt
    val minutes = stamps(1).toInt
    if(hours == 24)
      LocalTime.of(0,minutes,0)
    else
      LocalTime.of(hours,minutes,0)
  }

  def generateId(input : String) = {
    val md : MessageDigest = MessageDigest.getInstance("SHA-256")
    val messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8))
    val hexText = new BigInteger(1,messageDigest).toString(16)
    if(hexText.length < 32) "0"+hexText
    else hexText
  }

  def calculateAirTime(time : Int) = {
    (time/60)+" hours"+" "+(time % 60)+" minutes"
  }

  def generateBookingId(airline : String,flightNo : Int) = {
    airline + java.util.UUID.randomUUID().toString + flightNo
  }

  def generatePNR(input : String) = {
    input + java.util.UUID.randomUUID().toString.substring(0,3)
  }

  def calculatePrice(distance : Int) = {
    if(distance <= 1000) 30.0
    else if(distance > 1000 & distance <= 2000) 60.0
    else if(distance > 2000 & distance <=3000) 90.0
    else if(distance > 3000 & distance <= 4000) 120.0
    else 150.0
  }

  def ticketCancellationSuccess(bookingDetail: BookingDetails,bookingId : String) = {
    s"Flight booking for airline ${bookingDetail.ticket.airline} and flight no ${bookingDetail.ticket.flightNo} " +
      s"departing at ${bookingDetail.departureTime} from ${bookingDetail.ticket.source} having " +
      s"booking id ${bookingId} has been cancelled."
  }

  def ticketCancellationFailure(bookingId : String) = {
    s"Flight having booking Id ${bookingId} cannot be cancelled less than two hours of departure."
  }

  val FLIGHT_NOT_FOUND = "The flight details you are trying to book were not found"

  val FLIGHT_BOOKING_SUCCESSFUL = "Successfully booked ticket."

  val PAYMENT_INCOMPLETE = "PAYMENT_INCOMPLETE"
  val PAYMENT_SUCCESSFUL = "PAYMENT_SUCCESSFUL"
  val PAYMENT_FAILED = "PAYMENT_FAILED"

  def bookingNotFound(bookingId : String,customerId : Int) = s"Booking Id ${bookingId} not found for customer ${customerId}"

  def seatsFullMessage(ticket : Ticket) = {
    s"Seats full for airline ${ticket.airline} ,flight no ${ticket.flightNo} departing from ${ticket.source} to ${ticket.destination}"
  }

}
