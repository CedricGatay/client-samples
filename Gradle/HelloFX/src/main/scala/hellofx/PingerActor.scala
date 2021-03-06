package hellofx

import akka.actor.Actor

import scala.concurrent.duration._
import scala.util.Random

class PingerActor(pingerList: List[String]) extends Actor{

  implicit val ec = context.system.dispatcher

  override def receive: Receive = {
    case Ping =>
      val recipient = selectNextPinger()
      println(s"received Ping from ${sender().path.name}")
      context.system.scheduler.scheduleOnce(Random.nextInt(5).seconds){
        recipient ! Pong
      }
    case Pong =>
      val recipient = selectNextPinger()
      println(s"received Pong from ${sender().path.name}")
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
