import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import actors.{CurrencyLayerApiActor, FixerIoApiActor, RateActor}

class AppModule extends AbstractModule with AkkaGuiceSupport {
  def configure = {
    bindActor[RateActor]("rate-actor")
    bindActor[CurrencyLayerApiActor]("currency-layer-api-actor")
    bindActor[FixerIoApiActor]("fixer-io-api-actor")
  }
}
