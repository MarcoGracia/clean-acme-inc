package acme.inc

import akka.http.scaladsl.model.Uri
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus
import net.ceedubs.ficus.readers.{ArbitraryTypeReader, ValueReader}

object Config {
  import ArbitraryTypeReader._
  import Ficus._

  implicit val UriValueReader = new ValueReader[Uri] {
    import com.typesafe.config.Config

    override def read(config: Config, path: String) = {
      val value = config.getString(path)
      Uri(value)
    }
  }

  case class HttpConfig(interface: String, port: Int)
  case class MongoConfig(mongoUri: Uri)
  case class ActorSystem(enabled: Boolean)

  private val rootConfig = ConfigFactory.load()
  val config = rootConfig.getConfig("acme")

  val mongoConfig = config.as[MongoConfig]("database")
  val httpConfig = config.as[HttpConfig]("http")

}