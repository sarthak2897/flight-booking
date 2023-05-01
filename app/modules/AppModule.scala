package modules

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class AppModule extends AbstractModule with AkkaGuiceSupport{

  override def configure(): Unit = {
    bind(classOf[FlightApplication]).asEagerSingleton()
    bind(classOf[TicketBookingApplication]).asEagerSingleton()
  }
}
