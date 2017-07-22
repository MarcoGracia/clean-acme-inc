package acme.inc.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol


sealed trait AcmeApiModel

object AcmeApiModel extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val invoiceFormat = jsonFormat3(Invoice)
  implicit val adressFormat = jsonFormat5(Adress)
  implicit val userProfileFormat = jsonFormat3(UserProfile)
}

case class Invoice (
  number: String,
  date: Long,
  amount: Long
) extends AcmeApiModel

case class Adress (
  id: String,
  street: String,
  nr: Long,
  zipcode: String,
  invoices: List[Invoice]
) extends AcmeApiModel

case class UserProfile (
  id: Long,
  name: String,
  adresses: List[Adress]
) extends AcmeApiModel

case class MetaData (
  count: Long,
  ammount: Double
) extends AcmeApiModel

case class Invoices (
  meta: MetaData,
  adresses: List[Adress]
) extends AcmeApiModel