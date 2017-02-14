package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.RateActor.FetchLatest
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import models.Rate
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class RateController @Inject() (system: ActorSystem, @Named("rate-actor") rateActor: ActorRef) extends Controller {

  def get = Action.async {
    implicit val timeout: Timeout = 3 seconds

    (for {
      futureRate <- (rateActor ? FetchLatest).mapTo[Future[Rate]]
      rate <- futureRate
    } yield {
      Ok(Json.toJson(rate))
    }) recover {
      case e =>
        Logger.error(e.getMessage, e)

        InternalServerError
    }
  }
}
