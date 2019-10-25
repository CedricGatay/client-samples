package hellofx

import akka.actor.{ActorRef, ActorSystem, Props}
import org.graalvm.nativeimage.IsolateThread
import org.graalvm.nativeimage.c.function.CEntryPoint

import scala.collection.convert.ImplicitConversions.`seq AsJavaList`


class HelloFX {

  var system: ActorSystem = null
  var actors: Seq[ActorRef] = Seq()
  def init(): Unit = {
    import com.typesafe.config.ConfigFactory
    val customConf = ConfigFactory.parseString("""
               |akka {
               |  loggers = ["akka.event.slf4j.Slf4jLogger"]
               |  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
               |}
  """.stripMargin('|'))

    system = ActorSystem("gluon-mobile-akka", ConfigFactory.load(customConf))
    val pingerList = List("pinger1", "pinger2", "pinger3")
    val infoActor1 = system.actorOf(Props(classOf[PingerActor], pingerList), name = pingerList(0))
    val infoActor2 = system.actorOf(Props(classOf[PingerActor], pingerList), name = pingerList(1))
    val infoActor3 = system.actorOf(Props(classOf[PingerActor], pingerList), name = pingerList(2))

    actors ++= Seq(infoActor1, infoActor2, infoActor3)
    infoActor1.tell(Ping, sender = infoActor2)
  }

  def stop(): Unit ={
    //actors.foreach(e => println(e))
    system.stop(actors.head)
    system.stop(actors(1))
    system.stop(actors(2))
  }
}


object HelloFX {
  val instance = new HelloFX()
  def main(args: Array[String]): Unit = {
    instance.init()
    Thread.sleep(10 * 1000)
    instance.stop()
  }
}
