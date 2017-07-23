package acme.inc.dao

import acme.inc.model.UserProfile
import acme.inc.mongo.GenericDaoDb
import spray.json.JsonFormat

trait UserProfileDao extends GenericDaoDb[UserProfile] {
  protected[this] val jsonFormat = implicitly[JsonFormat[UserProfile]]
  protected[this] val collectionName = "UserProfile"

}

object UserProfileDao extends UserProfileDao
