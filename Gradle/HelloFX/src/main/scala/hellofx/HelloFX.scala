package hellofx

import akka.actor.{ActorSystem, Props}
import javafx.application.{Application, Platform}
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.VBox
import javafx.stage.Stage



class HelloFX extends Application {
  var label: Label = new Label("Empty label")
  override def start(stage: Stage): Unit = {
    val javaVersion = System.getProperty("java.version")
    val javafxVersion = System.getProperty("javafx.version")

    label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ", with " + util.Properties.versionMsg)
    label.setWrapText(true)
    val imageView = new ImageView(new Image(classOf[HelloFX].getResourceAsStream("/hellofx/openduke.png")))
    imageView.setFitHeight(200)
    imageView.setPreserveRatio(true)
    val root = new VBox(30, imageView, label)
    root.setAlignment(Pos.CENTER)
    val scene = new Scene(root, 640, 480)
    scene.getStylesheets.add(classOf[HelloFX].getResource("styles.css").toExternalForm)
    stage.setScene(scene)
    stage.show()
  }

  override def init(): Unit = {
    super.init()
    //FIXME CG this builds but transition to black screen on device :-(
    def logMessage = { msg: String =>
      Platform.runLater(new Runnable {
        override def run(): Unit = label.setText(msg)
      })
    }
    val system = ActorSystem("gluon-mobile-akka")
    val pingerList = List("pinger1", "pinger2", "pinger3")
    val infoActor1 = system.actorOf(Props(classOf[PingerActor], pingerList, logMessage), name = pingerList(0))
    val infoActor2 = system.actorOf(Props(classOf[PingerActor], pingerList, logMessage), name = pingerList(1))
    val _ = system.actorOf(Props(classOf[PingerActor], pingerList, logMessage), name = pingerList(2))

    infoActor1.tell(Ping, sender = infoActor2)
  }
}


object HelloFX {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[HelloFX], args: _*)
  }
}
