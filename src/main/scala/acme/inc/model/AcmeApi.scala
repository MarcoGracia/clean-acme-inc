package acme.inc.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

sealed trait AcmeApiModel

object AcmeApiModel extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val invoiceFormat = jsonFormat3(Invoice)
  implicit val newInvoiceFormat = jsonFormat2(NewInvoice)
  implicit val addressFormat = jsonFormat5(Address)
  implicit val userProfileFormat = jsonFormat3(UserProfile)
  implicit val metaDataFormat = jsonFormat2(MetaData)
  implicit val addressMetaFormat = jsonFormat2(AddressMeta)
  implicit val invoicesMetaFormat = jsonFormat2(InvoicesMeta)
}
/**
  * An invoice defines a payment for a client and an adrdress
  *
  * @param number  Invoice number.
  * @param date epoch timestamp when invoice was created
  * @param ammount payment ammount
  *
**/
case class Invoice (
  number: String,
  date: Long,
  ammount: Double
) extends AcmeApiModel

/**
  * Invoice entity without a date for backend injection
  *
  * @param number  Invoice number.
  * @param ammount payment ammount
  *
  **/
case class NewInvoice (
  number: String,
  ammount: Double
) extends AcmeApiModel

/**
  * Adress for a client
  *
  * @param id internal ID.
  * @param street
  * @param nr
  * @param zipcode
  * @param invoices List of invoices billed to this specific address
**/
case class Address (
  id: String,
  street: String,
  nr: Long,
  zipcode: String,
  invoices: List[Invoice]
) extends AcmeApiModel

/**
  * Contains all the user informaiton, including invoices
  *
  * @param id internal ID.
  * @param name
  * @param addresses List of addresses for this client, also contains invoice information
  **/
case class UserProfile (
  id: String,
  name: String,
  addresses: List[Address]
) extends AcmeApiModel

/**
  * Contains gathered data from
  *
  * @param count ammount of invoices described
  * @param ammount total invoice ammount
**/
case class MetaData (
  count: Long,
  ammount: Double
) extends AcmeApiModel

/**
  * Contains information for a single address
  *
  * @param meta ammount of invoices described
  * @param address contains the described address and its invoices
  **/
case class AddressMeta(
  meta: MetaData,
  address: Address
) extends AcmeApiModel

/**
  * Contains information for a set of addresses
  *
  * @param meta ammount of invoices described
  * @param addresses list of addresses with encapsulated data about their invoices
  **/
case class InvoicesMeta (
  meta: MetaData,
  addresses: List[AddressMeta]
) extends AcmeApiModel