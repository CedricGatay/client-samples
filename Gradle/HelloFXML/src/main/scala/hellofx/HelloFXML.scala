package hellofx

import akka.actor.{ActorSystem, Props}
import javafx.application.{Application, Platform}
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.{AnchorPane, VBox}
import javafx.stage.Stage
import java.io.IOException
import java.util
import java.util.ResourceBundle

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.{Image, ImageView}


object HelloFXML {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[HelloFXML], args: _*)
  }
}

class HelloFXML extends Application {
  @throws[IOException]
  override def start(stage: Stage): Unit = {
    val label = new Label("Hello")
    label.setWrapText(true)
    val root = new VBox(30, label)
    root.setAlignment(Pos.CENTER)
    val scene = new Scene(root, 640, 480)
    stage.setScene(scene)
    stage.show()
  }

  //FIXME CG this builds but transition to black screen on device :-(
  def logMessage = { msg: String =>
    Platform.runLater(new Runnable {
      override def run(): Unit = ()
    })
  }

  override def init(): Unit = {
    super.init()
    val system = ActorSystem("gluon-mobile-akka")
    val pingerList = List("pinger1", "pinger2", "pinger3")
    val infoActor1 = system.actorOf(Props(classOf[PingerActor], pingerList, logMessage), name = pingerList(0))
    val infoActor2 = system.actorOf(Props(classOf[PingerActor], pingerList, logMessage), name = pingerList(1))
    val _ = system.actorOf(Props(classOf[PingerActor], pingerList, logMessage), name = pingerList(2))

    infoActor1.tell(Ping, sender = infoActor2)
  }
}
