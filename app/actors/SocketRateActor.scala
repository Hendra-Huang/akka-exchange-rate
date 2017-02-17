package actors

import actors.RateActor._
import akka.actor.{Actor, ActorRef, Props}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class SocketRateActor(out: ActorRef, rateActor: ActorRef) extends Actor {
  private var watcher: ActorRef = null
  private val rateWatch = context.system.scheduler.schedule(0 millis, 60 seconds, rateActor, Watch)
  implicit val format = Json.format[RateUpdate]

  def receive = {
    case "subscribe" =>
      watcher = sender

    case rateUpdate: RateUpdate =>
      out ! Json.toJson(rateUpdate).toString
  }
}

object SocketRateActor {
  def props(out: ActorRef, rateActor: ActorRef) = Props(new SocketRateActor(out, rateActor))
}
