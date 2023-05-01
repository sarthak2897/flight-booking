package controllers

import MongoDao.FlightsRepository
import akka.stream.Materializer
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class FlightController @Inject()(implicit val mat : Materializer,
                                 implicit val ec : ExecutionContext,
                                 val flightsRepository: FlightsRepository,
                                 val controllerComponents: ControllerComponents) extends BaseController {

  final val logger : Logger = Logger(this.getClass)

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  //GET /flightRecordByAirline/:airlineName/:flightNo/:source/:destination controllers.FlightController.findFlightRecordByAirline(airlineName : String,flightNo : Int, source : String, destination : String)
//  def findFlightRecordByAirline(airlineName : String,flightNo : Int, source : String, destination : String): Action[AnyContent] =
//    Action.async{ implicit request: Request[AnyContent] =>
//    logger.info("Finding flight records for airline : "+airlineName)
//      flightsRepository
//        .findFlightRecordsById(airlineName,flightNo,source,destination)
//        .map(l => Ok(Json.toJson(l)))
//  }

  def findAllFlightRecords(limit : Int): Action[AnyContent] = Action.async{ implicit request =>
    logger.info("Searching first " + limit + " flight records")
    flightsRepository
      .findAllFlightRecords(limit)
      .map(l => Ok(Json.toJson(l)))
  }

  def totalFlightRecords() = Action.async{ implicit request =>
    logger.info("Counting all flight records")
    flightsRepository.totalRecords().map(l => Ok(Json.toJson(l))).recover(e => InternalServerError(Json.toJson(e.getMessage)))
  }
}
