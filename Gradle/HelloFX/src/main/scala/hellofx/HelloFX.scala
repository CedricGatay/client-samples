package hellofx

import java.util.function.Supplier

import akka.actor.{ActorSystem, Props}
import javafx.application.Application
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.{Control, Label}
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.stage.Stage



class HelloFX extends Application {
  override def start(stage: Stage): Unit = {
    val javaVersion = System.getProperty("java.version")
    val javafxVersion = System.getProperty("javafx.version")

    val label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ", with " + util.Properties.versionMsg)
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


    //FIXME CG as long as we don't use akka it builds natively
    //val system = ActorSystem("gluon-mobile-akka")
    val pingerList = List("pinger1", "pinger2", "pinger3")
   // val infoActor1 = system.actorOf(Props(classOf[PingerActor], pingerList), name = pingerList(0))
   // val infoActor2 = system.actorOf(Props(classOf[PingerActor], pingerList), name = pingerList(1))
    //val infoActor3 = system.actorOf(Props(new PingerActor(pingerList)), name = pingerList(2))

    //infoActor1.tell(Ping, sender = infoActor2)
  }
}


object HelloFX {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[HelloFX], args: _*)
  }
}
