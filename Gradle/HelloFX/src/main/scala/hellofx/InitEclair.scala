package hellofx

import java.io.{File, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.nio.file.{Files, Path, Paths}

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.util.ConfigLoader
import fr.acinq.eclair.{Kit, Setup}
import scodec.bits.ByteVector
import fr.acinq.eclair.blockchain.electrum.ElectrumEclairWallet
import fr.acinq.eclair.io.NodeURI
import fr.acinq.eclair.wire.NodeAddress
import org.bitcoin.{NativeSecp256k1, NativeSecp256k1Util}
import org.sqlite.SQLiteJDBCLoader

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class InitEclair {
import fr.acinq.eclair.io.NodeURI
  def init(): Unit ={

    println(s"java.vm.vendor ${System.getProperty("java.vm.vendor")}")

    /*println("Doing a simple https connection to earn.com")

    val url = new URL("https://bitcoinfees.earn.com/api/v1/fees/list")
    val con = url.openConnection.asInstanceOf[HttpURLConnection]
    con.setRequestMethod("GET")
    val status = con.getResponseCode
    println(s"Response ${status}")
    import java.io.BufferedReader
    val in = new BufferedReader(new InputStreamReader(con.getInputStream))
    val lines = new ArrayBuffer[String]()
    var line: String = null
    while ({line = in.readLine; line != null}) {
      lines.append(line)
    }
    con.disconnect

    println(s"${lines}")*/

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
        |}
        |
      """.stripMargin('|'))
    println("Creating dataDir ")
    val dataDir = Files.createTempDirectory("eclair")
    println(s"Created dataDir ${dataDir}")
    val fileExists = dataDir.toFile.exists()
    println(s"FileExists ${fileExists}")
    val path = Files.createFile(new File(dataDir.toFile, "eclair.conf").toPath)
    println(s"Created eclair.conf file ${path}")

    val system = ActorSystem("system", ConfigFactory.load(config))
    println(s"Created actor system ${system}")

    //SQLiteJDBCLoader.initialize()
    if ("The Android Project".equals(System.getProperty("java.vm.vendor"))){
      println("Running on 'mobile / graal boostrap' environment, loading libraries using java.library.path")
      System.loadLibrary("secp256k1")
      println("Loaded libsecp256k1")

      System.loadLibrary("sqlitejdbc")
      println("Loaded libsqlitejdbc")
    }
    Class.forName("org.sqlite.JDBC")
    println(s"Loaded JDBC")
    val seed = ByteVector.apply("this is a test".getBytes)
    println(s"Created seed ${seed}")
    val loadedConfig = ConfigFactory.load(config)
    println(s"Loaded configuration ${loadedConfig}")
    val setup = new Setup(dataDir.toFile, loadedConfig, Option.apply(seed), Option.empty)(system)
    println(s"Setup eclair done ${setup}")




    val host = "endurance.acinq.co"
    val port = "9735"
    try {
      println("Loaded JNI for Secp256k1")
      val ACINQ_NODE_URI: NodeURI = NodeURI.parse(nodeId + "@" + host + ":" + port)
      println(s"Set node coordinates ${ACINQ_NODE_URI}")
      setup.nodeParams.db.peers.addOrUpdatePeer(ACINQ_NODE_URI.nodeId, NodeAddress.fromParts(ACINQ_NODE_URI.address.getHost, ACINQ_NODE_URI.address.getPort).get)
      println(s"Added peer")
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    println("Bootstraping setup")
    val fKit = setup.bootstrap
    println(s"Bootstrapped Future ${fKit}")
    val kit = Await.result(fKit, Duration.create(60, "seconds"))
    println(s"Got Kit ${kit}")
    val electrumWallet = kit.wallet.asInstanceOf[ElectrumEclairWallet]
    println(s"Got Wallet ${electrumWallet}")

//    val appKit = new Kit(electrumWallet, kit)
  }
}
