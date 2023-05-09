package modules

import MongoDao.FlightsRepository
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{FileIO, Framing}
import akka.stream.{ActorAttributes, Materializer}
import akka.util.ByteString
import kafka.KafkaProducer
import modules.flows.AppFlows.{decider, insertFlightRecords, mappingFlow}
import play.api.Logger
import play.api.mvc.{BaseController, ControllerComponents}

import java.nio.file.Paths
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class FlightApplication @Inject()(val ac : ActorSystem,
                                  implicit val ec : ExecutionContext,
                                  implicit val mat : Materializer,
                                  val kafkaProducer: KafkaProducer,
                                  implicit val flightsRepository: FlightsRepository,
                                  val controllerComponents : ControllerComponents)
  extends BaseController {

  final val logger : Logger = Logger(this.getClass)

  logger.info("Processing flights data....")

  FileIO.fromPath(Paths.get("C:\\ticketbooking\\conf\\flights.csv"))
    .via(Framing.delimiter(ByteString("\n"),4096))
    .map(_.utf8String).drop(1)
    .via(mappingFlow)
    .mapAsync(10)(x => insertFlightRecords(x,flightsRepository))
    .map(kafkaProducer.produceFlightMessages)
    .withAttributes(ActorAttributes.supervisionStrategy(decider))
    .runWith(Producer.plainSink(kafkaProducer.producerSettings))
    .recover(e => logger.error("Error occurred : "+e.printStackTrace()))
    .onComplete(_ => logger.info("Completely processed flight data."))
}
