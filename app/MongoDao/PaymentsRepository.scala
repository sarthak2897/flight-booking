package MongoDao

import models.PaymentResponse
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PaymentsRepository @Inject() (implicit val ec : ExecutionContext,
                                    val reactiveMongoApi: ReactiveMongoApi) {

  def collection : Future[BSONCollection] = reactiveMongoApi.database.map(db => db.collection("payments"))

  def performPayment(payment : PaymentResponse) = {
    collection.flatMap(_.insert.one(payment))
  }

}
