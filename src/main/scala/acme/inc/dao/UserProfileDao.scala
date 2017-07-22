package acme.inc.dao

import acme.inc.model.{Address, UserProfile}
import acme.inc.mongo.GenericDaoDb
import acme.inc.mongo.MongoJsConverter._
import com.mongodb.casbah.Imports._
import spray.json.JsonFormat

import scala.concurrent._

trait UserProfileDao extends GenericDaoDb[UserProfile] {
  protected[this] val jsonFormat = implicitly[JsonFormat[UserProfile]]
  protected[this] val collectionName = "UserProfile"

}


object UserProfileDao extends UserProfileDao
