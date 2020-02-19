package hellofx

import java.io.{File, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.nio.file.Files

import akka.actor.{Actor, ActorSystem, Props}
import com.oracle.svm.core.{OS, SubstrateUtil}
import com.typesafe.config.{ConfigFactory, Optional}
import fr.acinq.eclair.{Kit, Setup}
import fr.acinq.eclair.blockchain.electrum.ElectrumEclairWallet
import fr.acinq.eclair.channel.ChannelEvent
import fr.acinq.eclair.db.BackupEvent
import fr.acinq.eclair.io.Peer.Connect
import fr.acinq.eclair.io.{NodeURI, Peer}
import fr.acinq.eclair.payment.PaymentEvent
import fr.acinq.eclair.router.SyncProgress
import fr.acinq.eclair.wire.NodeAddress
import hellofx.InitEclair.{kit, log}
import org.graalvm.nativeimage.CurrentIsolate
import org.graalvm.nativeimage.c.`type`.CTypeConversion
import scodec.bits.ByteVector

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import org.json4s._
import org.json4s.jackson.JsonMethods


class InitEclair {

}

object InitEclair {
  var kit: Option[Kit] = None

  def init(): Unit = {
    log(s"java.vm.vendor ${System.getProperty("java.vm.vendor")}")
    // log(ConfigFactory.defaultReference().getConfig("akka").getConfig("actor").getConfig("default-dispatcher").toString)
    log("Doing a simple https connection to earn.com")
    val url = new URL("https://bitcoinfees.earn.com/api/v1/fees/list")
    val con = url.openConnection.asInstanceOf[HttpURLConnection]
    con.setRequestMethod("GET")
    val status = con.getResponseCode
    log(s"Response ${status}")

    import java.io.BufferedReader
    val in = new BufferedReader(new InputStreamReader(con.getInputStream))
    val lines = new ArrayBuffer[String]()
    var line: String = null
    while ( {
      line = in.readLine;
      line != null
    }) {
      lines.append(line)
    }
    con.disconnect
    log(s"${lines}")

    /*
        {
          import scala.concurrent.ExecutionContext.Implicits.global
          import scala.concurrent.duration._
          implicit val sttpBackend: SttpBackend[Future, Nothing] = AkkaHttpBackend()

          implicit val timeout = Timeout(30 seconds)
          val provider = new EarnDotComFeeProvider()
          println("earn.com livenet fees: " + Await.result(provider.getFeerates, 10 seconds))
        }*/


    val nodeId = "03933884aaf1d6b108397e5efe5c86bcf2d8ca8d2f700eda99db9214fc2712b134"

    val config = ConfigFactory.parseString(
      s"""
         |eclair {
         |  chain = "testnet"
         |  local-features = "02" // data loss protect, and nothing else !
         |  override-features = [
         |    {
         |      nodeid = ${nodeId},
         |      global-features = "",
         |      local-features = "808a" // initial_routing_sync + option_data_loss_protect + option_channel_range_queries + option_channel_range_queries_ex
         |    }
         |  ]
         |
         |  router.path-finding.max-route-length = 4
         |  router.channel-exclude-duration = 10 seconds // 60s default is too long
         |  router.randomize-route-selection = false
         |
         |  watcher-type = "electrum"
         |
         |  min-feerate = 3
         |
         |  on-chain-fees {
         |    max-feerate-mismatch = 100 // large tolerance
         |  }
         |
         |  max-reconnect-interval = 20 seconds
         |}
         |akka {
         |  loggers = ["akka.event.slf4j.Slf4jLogger"]
         |  loglevel = "DEBUG"
         |  # ActorSystem start-up can be slow on overloaded phone, let's increase creation timeout for loggers actors.
         |  logger-startup-timeout = 10s
         |  log-dead-letters = "off"
         |  io {
         |    tcp {
         |      max-received-message-size = 65535b
         |    }
         |  }
         |  actor{
         |    default-dispatcher{
         |
         |    }
         |  }
         |}
         |
      """.stripMargin('|'))
    log("Creating dataDir ")
    val dataDir = Files.createTempDirectory("eclair")
    log(s"Created dataDir ${dataDir}")
    val fileExists = dataDir.toFile.exists()
    log(s"FileExists ${fileExists}")
    val path = Files.createFile(new File(dataDir.toFile, "eclair.conf").toPath)
    log(s"Created eclair.conf file ${path}")

    val system = ActorSystem("system", ConfigFactory.load(config))
    log(s"Created actor system ${system}")

    //SQLiteJDBCLoader.initialize()
    if ("The Android Project".equals(System.getProperty("java.vm.vendor"))) {
      log("Running on 'mobile / graal boostrap' environment, loading libraries using java.library.path")
      System.loadLibrary("secp256k1")
      log("Loaded libsecp256k1")

      System.loadLibrary("sqlitejdbc")
      log("Loaded libsqlitejdbc")
    }
    Class.forName("org.sqlite.JDBC")
    log(s"Loaded JDBC")
    val seed = ByteVector.apply("this is a test".getBytes)
    log(s"Created seed ${seed}")
    val loadedConfig = ConfigFactory.load(config)
    log(s"Loaded configuration ${loadedConfig}")
    val setup = new Setup(dataDir.toFile, loadedConfig, Option.apply(seed), Option.empty)(system)
    log(s"Setup eclair done ${setup}")


    val host = "endurance.acinq.co"
    val port = "9735"
    try {
      log("Loaded JNI for Secp256k1")
      val ACINQ_NODE_URI: NodeURI = NodeURI.parse(nodeId + "@" + host + ":" + port)
      log(s"Set node coordinates ${ACINQ_NODE_URI}")
      setup.nodeParams.db.peers.addOrUpdatePeer(ACINQ_NODE_URI.nodeId, NodeAddress.fromParts(ACINQ_NODE_URI.address.getHost, ACINQ_NODE_URI.address.getPort).get)
      log(s"Added peer")
    } catch {
      case e: Throwable => e.printStackTrace()
    }

    val nodeSupervisor = system.actorOf(Props.create(classOf[NodeSupervisor]), "NodeSupervisor")
    system.eventStream.subscribe(nodeSupervisor, classOf[BackupEvent])
    system.eventStream.subscribe(nodeSupervisor, classOf[ChannelEvent])
    system.eventStream.subscribe(nodeSupervisor, classOf[SyncProgress])
    system.eventStream.subscribe(nodeSupervisor, classOf[PaymentEvent])

    log("Bootstraping setup")
    val fKit = setup.bootstrap
    log(s"Bootstrapped Future ${fKit}")
    kit = Some(Await.result(fKit, Duration.create(60, "seconds")))
    log(s"Got Kit ${kit}")
    val electrumWallet = kit.get.wallet.asInstanceOf[ElectrumEclairWallet]
    log(s"Got Wallet ${electrumWallet}")

    log(s"Trying dummy switchServer")
    EclairMessage(Some(SwitchServer("http://this.is.a.test")))()
    log(s"Dummy switchServer called")

    log(s"Trying dummy switchServer parsing")
    val parsed = InitEclair.parseMessage(
      """{"switchServer":{"uri":"this.is.a.test"}}""".stripMargin)
    log(s"Dummy switchServer parsed ${parsed}")
  }

  def log(msg: String): Unit = {
    println(s"⚡ ${msg}")
    if (System.getProperty("NO_LOG") == "true") {
      return
    }
    val currentThread = CurrentIsolate.getCurrentThread
    /* Call a C function directly. */
    if (OS.getCurrent ne OS.WINDOWS) {
      /*
     * Calling C functions provided by the main executable from a shared library produced by
     * the native-image is not yet supported on Windows.
     */
      CInterfaceTutorial.printingInC(currentThread, CTypeConversion.toCString(msg).get())
    }
  }

  def message(msg: String): Unit = {
    log(s"Received message ${msg}")
    val maybeMessage = parseMessage(msg)
    log(s"MaybeMessage ${maybeMessage}")
    maybeMessage.foreach { m => m() }
    log("Message handling done")
  }

  implicit val formats = DefaultFormats

  def parseMessage(msg: String): Option[EclairMessage] = {
    JsonMethods.parse(msg).extractOpt[EclairMessage]
  }
}


case class EclairMessage(
                          switchServer: Option[SwitchServer] = None
                        ) {
  def apply(): Unit = {
    log(s"Applying SwitchServer ${switchServer}")
    if (!switchServer.isDefined) {
      log("No switchServer defined")
      return
    }
    log("Will try to change server")
    //03933884aaf1d6b108397e5efe5c86bcf2d8ca8d2f700eda99db9214fc2712b134@34.250.234.192:9735
    try {
      val nodeURI = NodeURI.parse(switchServer.get.uri)
      kit.get.switchboard.tell(Connect.apply(nodeURI), Actor.noSender)
      log("Asked to updated server")
    } catch {
      case e: Throwable => log(s"Error while trying to change server ${e}")
    }
  }
}

case class SwitchServer(uri: String)

import akka.actor.UntypedActor

class NodeSupervisor extends UntypedActor {
  override def onReceive(message: Any): Unit = {
    println(message)
  }
}



