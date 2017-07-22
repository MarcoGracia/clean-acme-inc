package acme.inc.dao

import acme.inc.model.{Adress, UserProfile}
import acme.inc.mongo.GenericDaoDb
import acme.inc.mongo.MongoJsConverter._
import com.mongodb.casbah.Imports._
import spray.json.JsonFormat

import scala.concurrent._

trait UserProfileDao extends GenericDaoDb[UserProfile] {
  protected[this] val jsonFormat = implicitly[JsonFormat[UserProfile]]
  protected[this] val collectionName = "UserProfile"

  def findAllByAdress(userId: String, adress: String)(implicit ec: ExecutionContext, db: MongoDB) = Future {
    val x = db(collectionName).find($and("_id" $eq userId, s"${collectionName}.adress" $eq adress))
    println("XXX", x)
    x
//      .map(_.toJsObject.convertTo[Adress])
//      .toList
  }

}


object UserProfileDaoe extends UserProfileDao
