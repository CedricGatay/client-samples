package hellofx

import java.nio.file.Files

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.util.ConfigLoader
import fr.acinq.eclair.{Kit, Setup}
import scodec.bits.ByteVector
import fr.acinq.eclair.blockchain.electrum.ElectrumEclairWallet
import fr.acinq.eclair.io.NodeURI
import fr.acinq.eclair.wire.NodeAddress

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class InitEclair {
import fr.acinq.eclair.io.NodeURI
  val ACINQ_NODE_URI: NodeURI = NodeURI.parse("03933884aaf1d6b108397e5efe5c86bcf2d8ca8d2f700eda99db9214fc2712b134@endurance.acinq.co:9735")
  def init(): Unit ={
    val config = ConfigFactory.parseString(
      """
        |eclair {
        |  chain = "testnet"
        |  local-features = "02" // data loss protect, and nothing else !
        |  override-features = [
        |    {
        |      nodeid = "03933884aaf1d6b108397e5efe5c86bcf2d8ca8d2f700eda99db9214fc2712b134",
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
        |  loglevel = "INFO"
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

    val system = ActorSystem("system", ConfigFactory.load(config))
    Class.forName("org.sqlite.JDBC")
    val dataDir = Files.createTempDirectory("eclair")
    val seed = ByteVector.apply("this is a test".getBytes)
    val setup = new Setup(dataDir.toFile, ConfigFactory.load(config), Option.apply(seed), Option.empty)(system)
    setup.nodeParams.db.peers.addOrUpdatePeer(ACINQ_NODE_URI.nodeId, NodeAddress.fromParts(ACINQ_NODE_URI.address.getHost, ACINQ_NODE_URI.address.getPort).get)

    val fKit = setup.bootstrap
    val kit = Await.result(fKit, Duration.create(60, "seconds"))
    val electrumWallet = kit.wallet.asInstanceOf[ElectrumEclairWallet]

//    val appKit = new Kit(electrumWallet, kit)
  }
}
