package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.RateActor.FetchLatest
import actors.SocketRateActor
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import models.Rate
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, WebSocket}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class RateController @Inject() (implicit system: ActorSystem, materializer: Materializer, @Named("rate-actor") rateActor: ActorRef) extends Controller {

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

  def realtime = Action.async {
    Future.successful {
      Ok("""
        <html>
          <head>
          </head>
          <body>
            <script>
              var ws = new WebSocket('ws://localhost:9000/rate-ws');
              ws.onopen = () => {
                console.log('I am open!')
                ws.send('subscribe')
              };
              ws.onmessage = m => {
                console.log(m)
              }
            </script>
          </body>
        </html>
      """).as("text/html")
    }
  }

  def rateSocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => SocketRateActor.props(out, rateActor))
  }
}
