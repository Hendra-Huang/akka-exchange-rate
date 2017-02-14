package actors

import akka.actor.{Actor, ActorLogging}
import javax.inject.Inject

import models.Rate
import play.api.Configuration
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CurrencyLayerApiActor @Inject() (config: Configuration, ws: WSClient) extends Actor with ActorLogging {
  import RateActor._

  private def getRates = {
    val accessKey = config.getString("currencylayerapi.accessKey").get
    val url = "http://apilayer.net/api/live"
    val request: WSRequest = ws.url(url)
      .withHeaders("Accept" -> "application/json")
      .withRequestTimeout(2 seconds)
      .withQueryString("access_key" -> accessKey, "currencies" -> "EUR,IDR,MYR,SGD,JPY,CNY")
    val futureResponse: Future[WSResponse] = request.get

    futureResponse
      .map[RateUpdate] { response =>
        val json = response.json
        val usdeur = (json \ "quotes" \ "USDEUR").as[Double]
        val usdidr = (json \ "quotes" \ "USDIDR").as[Double]
        val usdmyr = (json \ "quotes" \ "USDMYR").as[Double]
        val usdsgd = (json \ "quotes" \ "USDSGD").as[Double]
        val usdjpy = (json \ "quotes" \ "USDJPY").as[Double]
        val usdcny = (json \ "quotes" \ "USDCNY").as[Double]

        RateUpdate("currencyLayerApi", Rate(usdeur, usdidr, usdmyr, usdsgd, usdjpy, usdcny))
      }
  }

  def receive = {
    case FetchLatest => sender() ! getRates
  }
}

object CurrencyLayerApiActor {
}
