package kafka

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import kafka.KafkaConfig.{bootstrapServers, topic}
import models.FlightDetails
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import utils.Utils.mapper

import javax.inject.{Inject, Singleton}

@Singleton
class KafkaProducer @Inject() (implicit ac : ActorSystem) {


  val producerSettings = ProducerSettings(ac, new StringSerializer,new StringSerializer)
    .withBootstrapServers(bootstrapServers)

  val kafkaProducer = producerSettings.createKafkaProducer()

  def produceFlightMessages(flightDetails : FlightDetails) = {
    new ProducerRecord[String,String](topic,mapper.writeValueAsString(flightDetails))
  }


}
