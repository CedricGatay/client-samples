package hellofx

import akka.actor.{ActorSystem, Props}



class HelloFX {

  def init(): Unit = {
    import com.typesafe.config.ConfigFactory
    val customConf = ConfigFactory.parseString("""
               |akka {
               |  loggers = ["akka.event.slf4j.Slf4jLogger"]
               |  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
               |}
  """.stripMargin('|'))

    val system = ActorSystem("gluon-mobile-akka", ConfigFactory.load(customConf))
    val pingerList = List("pinger1", "pinger2", "pinger3")
    val infoActor1 = system.actorOf(Props(classOf[PingerActor], pingerList), name = pingerList(0))
    val infoActor2 = system.actorOf(Props(classOf[PingerActor], pingerList), name = pingerList(1))
    val _ = system.actorOf(Props(classOf[PingerActor], pingerList), name = pingerList(2))

    infoActor1.tell(Ping, sender = infoActor2)
  }
}


object HelloFX {
  def main(args: Array[String]): Unit = {
    new HelloFX().init()
  }
}
