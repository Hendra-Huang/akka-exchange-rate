package actors

import akka.actor.{Actor, ActorLogging}
import javax.inject.Inject

import models.Rate
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FixerIoApiActor @Inject() (ws: WSClient) extends Actor with ActorLogging {
  import RateActor._

  private def getRates = {
    val url = "http://api.fixer.io/latest"
    val request: WSRequest = ws.url(url)
      .withHeaders("Accept" -> "application/json")
      .withRequestTimeout(2 seconds)
      .withQueryString("base" -> "USD", "symbols" -> "EUR,IDR,MYR,SGD,JPY,CNY")
    val futureResponse: Future[WSResponse] = request.get

    futureResponse
      .map[RateUpdate] { response =>
        val json = response.json
        val usdeur = (json \ "rates" \ "EUR").as[Double]
        val usdidr = (json \ "rates" \ "IDR").as[Double]
        val usdmyr = (json \ "rates" \ "MYR").as[Double]
        val usdsgd = (json \ "rates" \ "SGD").as[Double]
        val usdjpy = (json \ "rates" \ "JPY").as[Double]
        val usdcny = (json \ "rates" \ "CNY").as[Double]

        RateUpdate("fixerIoApi", Rate(usdeur, usdidr, usdmyr, usdsgd, usdjpy, usdcny))
      }
  }

  def receive = {
    case FetchLatest => sender() ! getRates
  }
}

object FixerIoApiActor {
}
