package acme.inc.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol


sealed trait AcmeApiModel

object AcmeApiModel extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val invoiceFormat = jsonFormat3(Invoice)
  implicit val addressFormat = jsonFormat5(Address)
  implicit val userProfileFormat = jsonFormat3(UserProfile)
  implicit val metaDataFormat = jsonFormat2(MetaData)
  implicit val addressMetaFormat = jsonFormat2(AddressMeta)
  implicit val invoicesMetaFormat = jsonFormat2(InvoicesMeta)
}

case class Invoice (
  number: String,
  date: Long,
  ammount: Double
) extends AcmeApiModel

case class Address (
  id: String,
  street: String,
  nr: Long,
  zipcode: String,
  invoices: List[Invoice]
) extends AcmeApiModel

case class UserProfile (
  id: String,
  name: String,
  addresses: List[Address]
) extends AcmeApiModel

case class MetaData (
  count: Long,
  ammount: Double
) extends AcmeApiModel

case class AddressMeta(
  meta: MetaData,
  address: Address
) extends AcmeApiModel

case class InvoicesMeta (
  meta: MetaData,
  addresses: List[AddressMeta]
) extends AcmeApiModel