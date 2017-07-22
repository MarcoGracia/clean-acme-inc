package acme.inc.service

import acme.inc.model.UserProfile
import com.mongodb.casbah.MongoDB

import scala.concurrent.{ExecutionContext, Future}

trait AcmeService {
  import AcmeService._
  def getUser(userId: Long)(implicit ec: ExecutionContext, db: MongoDB) = Future(UserProfile(id = userId, name = "Hello world", adresses = Nil))
}

object AcmeService extends AcmeService