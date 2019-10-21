package hellofx

import akka.actor.{Actor, ActorLogging}

import scala.util.Random
import scala.concurrent.duration._

class PingerActor(pingerList: List[String]) extends Actor with ActorLogging {

  implicit val ec = context.system.dispatcher

  override def receive: Receive = {
    case Ping =>
      val recipient = selectNextPinger()
      log.info(s"received Ping from ${sender().path.name}")
      context.system.scheduler.scheduleOnce(Random.nextInt(5).seconds){
        recipient ! Pong
      }
    case Pong =>
      val recipient = selectNextPinger()
      log.info(s"received Pong from ${sender().path.name}")
      context.system.scheduler.scheduleOnce(Random.nextInt(5).seconds){
        recipient ! Ping
      }
  }

  def selectNextPinger() = {
    val nextPinger = Random.shuffle(pingerList.filterNot(_ == self.path.name)).head
    val basePath = self.path.root / "user" / nextPinger
    context.system.actorSelection(basePath)
  }

}

case object Ping
case object Pong
