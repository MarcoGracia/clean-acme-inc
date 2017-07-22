package acme.inc.route

import acme.inc.model.{Invoice, UserProfile}
import acme.inc.service.AcmeService
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import akka.util.Timeout
import com.mongodb.casbah.Imports.MongoDB
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait AcmeRoute extends Directives with DefaultJsonProtocol {
  implicit def system: ActorSystem

  implicit def ec: ExecutionContext

  implicit def materializer: Materializer

  implicit def db: MongoDB

  private val acmeService = AcmeService

  //def managerActor: ActorRef

  import akka.http.scaladsl.unmarshalling.Unmarshaller

  private implicit val timeout = Timeout(10.seconds) // TODO: make configurable

  val acmeRoute = pathPrefix("api") {
    pathPrefix("customer" / Segment) { userId =>
      pathPrefix("profile") {
        get {
          complete {
            acmeService.getUserData(userId)
          }
        } ~
        post {
          entity(as[UserProfile]) { user =>
            complete {
              acmeService.createUser(user)
            }
          }
        }
      } ~
      pathPrefix("invoices") {
        pathPrefix("address" / Segment) { addressId =>
          get {
            complete {
              acmeService.getAllInvoicesForAddress(userId, addressId)
            }
          } ~
          post {
            entity(as[Invoice]) { invoice =>
              complete {
                acmeService.addInvoice(userId, addressId, invoice)
              }
            }
          }
        } ~
        get{
          parameter("from".as[Long], "to".as[Long]) { (from, to) =>
            complete {
              acmeService.getAllInvoicesFromPeriod(userId, from, to)
            }
          }
        } ~
        get {
          complete {
            //acmeService.getAllInvoices(userId)
            acmeService.getUser(userId)
          }
        }
      }
    }
  }
}