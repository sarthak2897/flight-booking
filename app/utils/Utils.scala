package utils

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalTime

object Utils {

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

}
