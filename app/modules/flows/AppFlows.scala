package modules.flows

import akka.NotUsed
import akka.stream.Supervision
import akka.stream.scaladsl.Flow
import models.FlightDetails
import play.api.Logger
import reactivemongo.core.errors.DatabaseException
import utils.Utils.{calculateAirTime, calculatePrice, formatTime, generateId, generatePNR}

import scala.util.Try

object AppFlows {

  final val logger : Logger = Logger(AppFlows.getClass)

  val mappingFlow: Flow[String, FlightDetails, NotUsed] = Flow[String].async
    .map(line => toFlightDetails(line.split(",").toList))


  def toFlightDetails(flightsList : List[String]) = {
   FlightDetails(id = generateId(flightsList(4)+flightsList(5)+flightsList(7) +flightsList(8)),
     airline = flightsList(4),
     flightNo = flightsList(5).toInt,
     tailNo = flightsList(6),
     origin = flightsList(7),
     destination = flightsList(8),
     departureTime = formatTime(flightsList(10)),
     arrivalTime = formatTime(flightsList(20)),
     airTime = Try(calculateAirTime(flightsList(16).toInt)).getOrElse("0 mins"),
     distance = flightsList(17)+" km",
     pnr = generatePNR(flightsList(4)+flightsList(5)),
     price = calculatePrice(flightsList(17).toInt))
  }

  val decider: Supervision.Decider = {
    case e : DatabaseException if e.code.get == 11000 =>
      logger.debug("Duplicate Key Warning: "+e.printStackTrace())
      Supervision.Resume
    case e : Exception =>
      logger.error("Error occurred: " + e.printStackTrace())
      Supervision.Stop
  }
}
