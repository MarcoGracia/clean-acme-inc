package acme.inc


import acme.inc.route.AcmeRoute
import akka.actor._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.mongodb.casbah.Imports._



object Boot extends App with AcmeRoute  {
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  private val mongoUri = MongoClientURI(Config.mongoConfig.mongoUri.toString())
  implicit val db = MongoClient(mongoUri).getDB(mongoUri.database.get)

  println(s"Starting acme service")

  Http().bindAndHandle(acmeRoute, Config.httpConfig.interface, Config.httpConfig.port).transform(
    binding => println(s"REST interface bound to ${binding.localAddress} "), { t => println(s"Couldn't bind interface: ${t.getMessage}", t); sys.exit(1) }
  )


}
