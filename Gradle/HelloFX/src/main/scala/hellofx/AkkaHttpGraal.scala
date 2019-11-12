package hellofx

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

object Main extends LazyLogging {

  def main(args: Array[String]): Unit = {
    println("AZEAZEAZEAZEQSDQSDQSDQSDQSD")
    logger.info(s"Running app")
    val properties = System.getProperties
    properties.forEach((k: Any, v: Any) => System.out.println(k + ":" + v))
    InitEclair.init()
    val customConf = ConfigFactory.parseString("""
                                                 |akka {
                                                 |  loggers = ["akka.event.slf4j.Slf4jLogger"]
                                                 |  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
                                                 |}
                                                 |http {
                                                 |  service {
                                                 |    port = 8086
                                                 |    bind-to = "0.0.0.0"
                                                 |  }
                                                 |}
                                               """.stripMargin('|'))
    val config = ConfigFactory.load(customConf)
    implicit val system: ActorSystem = ActorSystem("graal", config)
    implicit val materializer: Materializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    val route =
      path("graal-hp-size") {
        get {
          onSuccess(graalHomepageSize) { size =>
            complete(size.toString)
          }
        }
      }

    Http()
      .bindAndHandle(route,
        config.getString("http.service.bind-to"),
        config.getInt("http.service.port"))
      .andThen {
        case Success(binding) => logger.info(s"Listening at ${binding.localAddress}")
      }
  }

  private def graalHomepageSize(implicit ec: ExecutionContext,
                                system: ActorSystem,
                                mat: Materializer): Future[Int] =
    Http().singleRequest(HttpRequest(uri = "http://www.google.fr")).flatMap { resp =>
      resp.status match {
        case StatusCodes.OK =>
          resp.entity.dataBytes.runFold(0) { (cnt, chunk) =>
            cnt + chunk.size
          }
        case other =>
          resp.discardEntityBytes()
          throw new IllegalStateException(s"Unexpected status code $other")
      }

    }
}
