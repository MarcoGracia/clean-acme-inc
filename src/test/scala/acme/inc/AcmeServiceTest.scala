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
      val invoice = NewInvoice(number = "1", ammount = 350)
      val invoice2 = NewInvoice(number = "2", ammount = 5000)

      val user = UserProfile(id = "1", name = "marco gracia", addresses = addresses)


      acmeService.createUser(user)(ec, db).awaitResult
      val i1 = acmeService.addInvoice(user.id, "1", invoice)(ec, db).awaitResult
      val i2 = acmeService.addInvoice(user.id, "1", invoice2)(ec, db).awaitResult
      val i3 = acmeService.addInvoice(user.id, "2", invoice2)(ec, db).awaitResult

      val addresses2 =
        List(
          Address(id = "2", street = "Street 2", nr = 2, zipcode = "Zipcode 2", invoices = List(i3)),
          Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = List(i2, i1))
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
      val invoice = NewInvoice(number = "1", ammount = 350)
      val user = UserProfile(id = "1", name = "marco gracia", addresses = addresses)


      acmeService.createUser(user)(ec, db).awaitResult
      val i1 = acmeService.addInvoice(user.id, "1", invoice)(ec, db).awaitResult
      val fUser = acmeService.getUser("1")(ec, db).awaitResult

      fUser should not be empty
      fUser.get.addresses.find(_.id == "1").get.invoices.head mustEqual i1
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
      val invoice = NewInvoice(number = "1", ammount = 350)
      val invoice2 = NewInvoice(number = "2", ammount = 5000)

      val user = UserProfile(id = "1", name = "marco gracia", addresses = addresses)


      acmeService.createUser(user)(ec, db).awaitResult
      val i1 = acmeService.addInvoice(user.id, "1", invoice)(ec, db).awaitResult
      val i2 = acmeService.addInvoice(user.id, "1", invoice2)(ec, db).awaitResult
      val i3 = acmeService.addInvoice(user.id, "2", invoice2)(ec, db).awaitResult

      val invoices = acmeService.getAllInvoices(user.id)(ec, db).awaitResult

      invoices should not be empty
      invoices must containTheSameElementsAs(List(i1, i2, i3))
    }

    // 1. As a customer I want to see all the invoices for a specific address
    "get all invoices for address" in new DefaultContext with FutureTestSupport {
      val addresses =
        List(
          Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = Nil),
          Address(id = "2", street = "Street 2", nr = 2, zipcode = "Zipcode 2", invoices = Nil),
          Address(id = "3", street = "Street 3", nr = 3, zipcode = "Zipcode 3", invoices = Nil)
        )

      val invoice = NewInvoice(number = "1", ammount = 1)
      val invoice2 = NewInvoice(number = "2", ammount = 2)
      val invoice3 = NewInvoice(number = "3", ammount = 100)
      val invoice4 = NewInvoice(number = "4", ammount = 3)
      val invoice5 = NewInvoice(number = "5", ammount = 5)

      val user = UserProfile(id = "1", name = "marco gracia", addresses = addresses)

      acmeService.createUser(user)(ec, db).awaitResult
      val i1 = acmeService.addInvoice(user.id, "1", invoice)(ec, db).awaitResult
      val i2 = acmeService.addInvoice(user.id, "1", invoice2)(ec, db).awaitResult
      val i3 = acmeService.addInvoice(user.id, "1", invoice3)(ec, db).awaitResult
      val i4 = acmeService.addInvoice(user.id, "1", invoice4)(ec, db).awaitResult
      val i5 = acmeService.addInvoice(user.id, "2", invoice2)(ec, db).awaitResult
      val i6 = acmeService.addInvoice(user.id, "3", invoice5)(ec, db).awaitResult


      val invoicesMD = acmeService.getAllInvoicesForAddress(user.id, "1")(ec, db).awaitResult
      val metaData =
        AddressMeta(
          meta = MetaData(count = 4, ammount = 106),
          address = Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = List(i4, i3, i2, i1))
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

      val invoice = NewInvoice(number = "1", ammount = 1)
      val invoice2 = NewInvoice(number = "2", ammount = 2)
      val invoice3 = NewInvoice(number = "3", ammount = 100)
      val invoice4 = NewInvoice(number = "4", ammount = 3)
      val invoice5 = NewInvoice(number = "5", ammount = 5)

      val user = UserProfile(id = "1", name = "marco gracia", addresses = addresses)


      acmeService.createUser(user)(ec, db).awaitResult
      val i1 = acmeService.addInvoice(user.id, "1", invoice)(ec, db).awaitResult
      val i2 = acmeService.addInvoice(user.id, "1", invoice2)(ec, db).awaitResult
      val i3 = acmeService.addInvoice(user.id, "1", invoice3)(ec, db).awaitResult
      val i4 = acmeService.addInvoice(user.id, "1", invoice4)(ec, db).awaitResult
      val i5 = acmeService.addInvoice(user.id, "2", invoice2)(ec, db).awaitResult
      val i6 = acmeService.addInvoice(user.id, "3", invoice5)(ec, db).awaitResult



      val minDate = List(i1, i2, i3, i4, i5, i6).map(_.date).min
      val maxDate = List(i1, i2, i3, i4, i5, i6).map(_.date).max

      // include all
      val invoicesMD = acmeService.getAllInvoicesFromPeriod(user.id, minDate, maxDate)(ec, db).awaitResult
      val metaData =
        InvoicesMeta(
          meta = MetaData(count = 6, ammount = 113),
          addresses = List(
            AddressMeta(
              address = Address(id = "3", street = "Street 3", nr = 3, zipcode = "Zipcode 3", invoices = List(i6)),
              meta = MetaData(count = 1, ammount = 5)
            ),
            AddressMeta(
              address = Address(id = "2", street = "Street 2", nr = 2, zipcode = "Zipcode 2", invoices =  List(i5)),
              meta = MetaData(count = 1, ammount = 2)
            ),
            AddressMeta(
              address = Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = List(i4, i3, i2, i1)),
              meta = MetaData(count = 4, ammount = 106)
            )
          )
        )
      invoicesMD mustEqual metaData

      // invoice 6, 5 and 4  and address 2 and 3 should be excluded
      val invoicesMD2 = acmeService.getAllInvoicesFromPeriod(user.id, i1.date, i3.date)(ec, db).awaitResult

      val metaData2 =
        InvoicesMeta(
          meta = MetaData(count = 3, ammount = 103),
          addresses = List(
            AddressMeta(
              address = Address(id = "1", street = "Street 1", nr = 1, zipcode = "Zipcode 1", invoices = List(i3, i2, i1)),
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


