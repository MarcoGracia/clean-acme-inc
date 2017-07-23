package acme.inc.service

import acme.inc.model._
import acme.inc.dao.UserProfileDao
import com.mongodb.casbah.MongoDB

import scala.concurrent.{ExecutionContext, Future}



trait AcmeService {
  val userProfileDao = UserProfileDao

  def createUser(user: UserProfile)(implicit ec: ExecutionContext, db: MongoDB): Future[UserProfile] = {
    userProfileDao.insert(user).map(_ => user)
  }

  // 1. As a customer I want to see an overview of all my addresses and personal data
  def getUserData(userId: String)(implicit ec: ExecutionContext, db: MongoDB) = {
    checkUser(userId).map{x => x.copy(addresses = x.addresses.map(_.copy(invoices = Nil)))
    }
  }

  //2. As a system website I want to create an invoice
  def addInvoice(userId: String, addressId: String ,invoice: NewInvoice)(implicit ec: ExecutionContext, db: MongoDB): Future[Invoice] = {
    checkUser(userId).flatMap { user =>
      user.addresses.find(_.id == addressId).map(address => (address, user)) match {
        case Some((address, user)) =>
          val updatedInvoice = Invoice(number = invoice.number, ammount = invoice.ammount, date = now())
          val fixedAdress = address.copy(invoices = updatedInvoice :: address.invoices)
          val updatedUser = user.copy(addresses = fixedAdress :: user.addresses.filterNot(_.id == address.id))
          userProfileDao.update(userId, updatedUser).map(_ => updatedInvoice)
        case None =>
          throw new IllegalArgumentException(s"Address: $addressId not found")

      }
    }
  }

  //3. As a customer I want to see all my invoices.
  def getUser(userId: String)(implicit ec: ExecutionContext, db: MongoDB) = userProfileDao.findOne(userId)

  // Only invoices
  def getAllInvoices(userId: String)(implicit ec: ExecutionContext, db: MongoDB): Future[List[Invoice]] = {
    checkUser(userId).map (_.addresses.flatMap(_.invoices))
  }

  // 1. As a customer I want to see all the invoices for a specific address
  def getAllInvoicesForAddress(userId: String, addressId: String)(implicit ec: ExecutionContext, db: MongoDB): Future[AddressMeta] = {
    checkUser(userId).map {
      _.addresses.find(_.id == addressId) match {
        case Some(address) =>
          AddressMeta(
            address = address,
            meta = MetaData(count = address.invoices.length, ammount = address.invoices.map(_.ammount).sum)
          )
        case None =>
          throw new IllegalArgumentException(s"Address: $addressId not found")
      }
    }
  }

  // 2. As a customer I want to see a summary with count and total amount of the invoices I get in a
  // given time period
  def getAllInvoicesFromPeriod(userId: String, from: Long, to: Long)(implicit ec: ExecutionContext, db: MongoDB): Future[InvoicesMeta] = {
    checkUser(userId).map { user =>
      val addresses = user.addresses.filter(a => a.invoices.exists(i => i.date >= from & i.date <= to)).map { address =>
        val collAddress = address.copy(invoices = address.invoices.filter(i => i.date >= from & i.date <= to))
          AddressMeta(
            address = collAddress,
            meta = MetaData(count = collAddress.invoices.length, ammount = collAddress.invoices.map(_.ammount).sum)
          )
      }
      InvoicesMeta(
        addresses = addresses,
        meta = MetaData(count = addresses.map(_.meta.count).sum, ammount = addresses.map(_.meta.ammount).sum)
      )
    }
  }

  private def checkUser(userId: String)(implicit ec: ExecutionContext, db: MongoDB): Future[UserProfile] = {
    getUser(userId).map {
      _ match {
        case Some(u) =>
          u
        case None =>
          throw new IllegalArgumentException(s"User: $userId not found")
      }
    }
  }

  private def now() = System.currentTimeMillis()

}

object AcmeService extends AcmeService