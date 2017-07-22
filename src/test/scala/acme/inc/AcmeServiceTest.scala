package acme.inc

import java.util.UUID

import acme.inc.model._
import acme.inc.service.AcmeService
import com.mongodb.casbah.MongoDB
import org.specs2.mutable.Specification
import util.MongoContext
import util.FutureTestSupport

import scala.concurrent.{Await, ExecutionContext, Future}

class AcmeServiceTest extends Specification {
  "AcmeService" should {
    // 1. As a customer I want to see an overview of all my addresses and personal data
    "get user" in new DefaultContext with FutureTestSupport {
      val addresses =
        List(
          Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = Nil),
          Address(id = "2", street = "Street 2", nr = 2, zipcode = "Zipcode 2", invoices = Nil)
        )
      val invoice = Invoice(number = "1", date = 1L, ammount = 350)
      val invoice2 = Invoice(number = "2", date = 2L, ammount = 5000)

      val user = UserProfile(id = "1", name = "marco gracia", addresses = addresses)


      acmeService.createUser(user)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "1", invoice)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "1", invoice2)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "2", invoice2)(ec, db).awaitResult

      val addresses2 =
        List(
          Address(id = "2", street = "Street 2", nr = 2, zipcode = "Zipcode 2", invoices = List(invoice2)),
          Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = List(invoice2, invoice))
        )
      val updatedUser = user.copy(addresses = addresses2)

      val fUser = acmeService.getUser("1")(ec, db).awaitResult
      fUser mustEqual Some(updatedUser)

    }

    //2. As a system website I want to create an invoice
    "add invoice" in new DefaultContext with FutureTestSupport {
      val addresses =
        List(
          Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = Nil),
          Address(id = "2", street = "Street 2", nr = 2, zipcode = "Zipcode 2", invoices = Nil)
        )
      val invoice = Invoice(number = "1", date = 1L, ammount = 350)
      val user = UserProfile(id = "1", name = "marco gracia", addresses = addresses)


      acmeService.createUser(user)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "1", invoice)(ec, db).awaitResult
      val fUser = acmeService.getUser("1")(ec, db).awaitResult

      fUser should not be empty
      fUser.get.addresses.find(_.id == "1").get.invoices.head mustEqual invoice
    }

    //3. As a customer I want to see all my invoices.
    "get user data" in new DefaultContext with FutureTestSupport {
      val user = UserProfile(id = "1", name = "marco gracia", addresses = Nil)
      acmeService.createUser(user)(ec, db).awaitResult
      val fUser = acmeService.getUserData("1")(ec, db).awaitResult
      fUser mustEqual user

    }

    //3. As a customer I want to see all my invoices. (raw)
    "get all invoices" in new DefaultContext with FutureTestSupport {
      val addresses =
        List(
          Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = Nil),
          Address(id = "2", street = "Street 2", nr = 2, zipcode = "Zipcode 2", invoices = Nil)
        )
      val invoice = Invoice(number = "1", date = 1L, ammount = 350)
      val invoice2 = Invoice(number = "2", date = 2L, ammount = 5000)

      val user = UserProfile(id = "1", name = "marco gracia", addresses = addresses)


      acmeService.createUser(user)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "1", invoice)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "1", invoice2)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "2", invoice2)(ec, db).awaitResult

      val invoices = acmeService.getAllInvoices(user.id)(ec, db).awaitResult

      invoices should not be empty
      println(invoices)
      invoices must containTheSameElementsAs(List(invoice, invoice2, invoice2))
    }

    // 1. As a customer I want to see all the invoices for a specific address
    "get all invoices for address" in new DefaultContext with FutureTestSupport {
      val addresses =
        List(
          Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = Nil),
          Address(id = "2", street = "Street 2", nr = 2, zipcode = "Zipcode 2", invoices = Nil),
          Address(id = "3", street = "Street 3", nr = 3, zipcode = "Zipcode 3", invoices = Nil)
        )

      val invoice = Invoice(number = "1", date = 1L, ammount = 1)
      val invoice2 = Invoice(number = "2", date = 2L, ammount = 2)
      val invoice3 = Invoice(number = "3", date = 3L, ammount = 100)
      val invoice4 = Invoice(number = "4", date = 4L, ammount = 3)
      val invoice5 = Invoice(number = "5", date = 25L, ammount = 5)

      val user = UserProfile(id = "1", name = "marco gracia", addresses = addresses)

      acmeService.createUser(user)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "1", invoice)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "1", invoice2)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "1", invoice3)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "1", invoice4)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "2", invoice2)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "3", invoice5)(ec, db).awaitResult


      val invoicesMD = acmeService.getAllInvoicesForAddress(user.id, "1")(ec, db).awaitResult
      val metaData =
        AddressMeta(
          meta = MetaData(count = 4, ammount = 106),
          address = Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = List(invoice4, invoice3, invoice2, invoice))
        )
      invoicesMD mustEqual metaData

    }


    // 2. As a customer I want to see a summary with count and total amount of the invoices I get in a
    // given time period
    "get all invoices for period" in new DefaultContext with FutureTestSupport {
      val addresses =
        List(
          Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = Nil),
          Address(id = "2", street = "Street 2", nr = 2, zipcode = "Zipcode 2", invoices = Nil),
          Address(id = "3", street = "Street 3", nr = 3, zipcode = "Zipcode 3", invoices = Nil)
        )

      val invoice = Invoice(number = "1", date = 1L, ammount = 1)
      val invoice2 = Invoice(number = "2", date = 2L, ammount = 2)
      val invoice3 = Invoice(number = "3", date = 3L, ammount = 100)
      val invoice4 = Invoice(number = "4", date = 4L, ammount = 3)
      val invoice5 = Invoice(number = "5", date = 25L, ammount = 5)

      val user = UserProfile(id = "1", name = "marco gracia", addresses = addresses)


      acmeService.createUser(user)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "1", invoice)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "1", invoice2)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "1", invoice3)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "1", invoice4)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "2", invoice2)(ec, db).awaitResult
      acmeService.addInvoice(user.id, "3", invoice5)(ec, db).awaitResult


      // include all
      val invoicesMD = acmeService.getAllInvoicesFromPeriod(user.id, 1L, 26L)(ec, db).awaitResult
      val metaData =
        InvoicesMeta(
          meta = MetaData(count = 6, ammount = 113),
          addresses = List(
            AddressMeta(
              address = Address(id = "3", street = "Street 3", nr = 3, zipcode = "Zipcode 3", invoices = List(invoice5)),
              meta = MetaData(count = 1, ammount = 5)
            ),
            AddressMeta(
              address = Address(id = "2", street = "Street 2", nr = 2, zipcode = "Zipcode 2", invoices =  List(invoice2)),
              meta = MetaData(count = 1, ammount = 2)
            ),
            AddressMeta(
              address = Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = List(invoice4, invoice3, invoice2, invoice)),
              meta = MetaData(count = 4, ammount = 106)
            )
          )
        )
      invoicesMD mustEqual metaData

      // invoice 5 and 4 should be excluded
      val invoicesMD2 = acmeService.getAllInvoicesFromPeriod(user.id, 1L, 3L)(ec, db).awaitResult

      val metaData2 =
        InvoicesMeta(
          meta = MetaData(count = 4, ammount = 105),
          addresses = List(
            AddressMeta(
              address = Address(id = "2", street = "Street 2", nr = 2, zipcode = "Zipcode 2", invoices =  List(invoice2)),
              meta = MetaData(count = 1, ammount = 2)
            ),
            AddressMeta(
              address = Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = List(invoice3, invoice2, invoice)),
              meta = MetaData(count = 3, ammount = 103)
            )
          )
        )
      invoicesMD2 mustEqual metaData2

    }
  }
}

trait DefaultContext extends MongoContext {
  implicit def db: MongoDB = getDb()

  def ec: ExecutionContext = ExecutionContext.Implicits.global
  val acmeService = AcmeService
}


