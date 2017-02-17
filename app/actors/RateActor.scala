package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import models.Rate
import javax.inject.{Inject, Named}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class RateActor @Inject() (
                            @Named("currency-layer-api-actor") currencyLayerApi: ActorRef,
                            @Named("fixer-io-api-actor") fixerIoApi: ActorRef
                          ) extends Actor with ActorLogging {
  import RateActor._

  def receive = {
    case FetchLatest =>
      implicit val timeout: Timeout = 2 seconds

      val futureCurrencyLayerRate: Future[Rate] = for {
        futureRateUpdate <- (currencyLayerApi ? FetchLatest).mapTo[Future[RateUpdate]]
        rateUpdate <- futureRateUpdate
      } yield {
        rateUpdate match {
          case RateUpdate(_, rate) => rate
        }
      }
      val futureFixerIoRate: Future[Rate] = for {
        futureRateUpdate <- (fixerIoApi ? FetchLatest).mapTo[Future[RateUpdate]]
        rateUpdate <- futureRateUpdate
      } yield {
        rateUpdate match {
          case RateUpdate(_, rate) => rate
        }
      }
//      val futureCurrencyLayerRate: Future[Rate] = (currencyLayerApi ? FetchLatest).mapTo[Future[RateUpdate]]
//        .flatMap { futureRate =>
//          futureRate map[Rate] {
//            case RateUpdate(_, rate) => rate
//          }
//        }
//      val futureFixerIoRate: Future[Rate] = (fixerIoApi ? FetchLatest).mapTo[Future[RateUpdate]]
//        .flatMap { futureRate =>
//          futureRate map[Rate] {
//            case RateUpdate(_, rate) => rate
//          }
//        }
      val futureAggregateRate: Future[Rate] = for {
        currencyLayerRate <- futureCurrencyLayerRate
        fixerIoRate <- futureFixerIoRate
      } yield {
        Rate(
          math.min(currencyLayerRate.usdeur, fixerIoRate.usdeur),
          math.min(currencyLayerRate.usdidr, fixerIoRate.usdidr),
          math.min(currencyLayerRate.usdmyr, fixerIoRate.usdmyr),
          math.min(currencyLayerRate.usdsgd, fixerIoRate.usdsgd),
          math.min(currencyLayerRate.usdjpy, fixerIoRate.usdjpy),
          math.min(currencyLayerRate.usdcny, fixerIoRate.usdcny)
        )
      }

      sender() ! futureAggregateRate

    case Watch =>
      implicit val timeout: Timeout = 2 seconds
      val out = sender

      (currencyLayerApi ? FetchLatest).mapTo[Future[RateUpdate]]
        .flatMap { futureRate =>
          futureRate map { rate =>
            out ! rate
          }
        }
      (fixerIoApi ? FetchLatest).mapTo[Future[RateUpdate]]
        .flatMap { futureRate =>
          futureRate map { rate =>
            out ! rate
          }
        }

  }
}

object RateActor {
  case object FetchLatest
  case class RateUpdate(provider: String, rate: Rate)
  case object Watch
}
