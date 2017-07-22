package acme.inc.mongo


import acme.inc.mongo.MongoJsConverter._
import com.mongodb.WriteResult
import com.mongodb.casbah.Imports._
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

/**
  * A helper trait to use the generic dao abstraction with String typed Ids
  */
trait GenericDaoDb[T] extends GenericDaoDbForId[T, String]

/**
  * A trait that provides a generic DAO abstraction for access to the database. It works on case
  * classes and takes care of the database layer operations.
  *
  * All operations are non blocking and future based and expect an execution context to be run in.
  *
  * For use a set of defined members has to be overridden to provide the configuration, see the
  * documentation at the relevant members.
  *
  * The case class requires an 'id' field.
  */
trait GenericDaoDbForId[T, ID] {

  /**
    * A JsonFormat for the type class. Currently integrated here because traits do not support
    * context extensions, e.g. [T : JsonFormat] in definition.
    */
  protected[this] implicit def jsonFormat: JsonFormat[T]

  /**
    * The name of the collection where the operations are taking place.
    */
  protected[this] def collectionName: String

  private def coll(implicit db: MongoDB) = db(collectionName)

  private def identityObject(id: ID) = DBObject("_id" -> id)

  /**
    * Reads the DBObject and returns the typed entity, can be override by subclasses if custom behaviour for case
    * classes, like special IDs, are required.
    *
    * @param dbo
    * @return
    */
  protected[this] def dboToEntity(dbo: DBObject): T = dbo.toJsObject.convertTo[T]

  /**
    * Converts the entity to DBObject, can be override by subclasses if custom behaviour for case
    * classes, like special IDs, are required.
    *
    * @param t
    * @return
    */
  protected[this] def entityToDbo(t: T): DBObject = t.toJson.toDbObject

  /**
    * Inserts the object into the database.
    */
  def insert(t: T)(implicit ec: ExecutionContext, db: MongoDB) = Future {
     coll.insert(entityToDbo(t))
  }

  /**
    * Inserts the object into the database.
    */
  def insertBulk(ts: List[T])(implicit ec: ExecutionContext, db: MongoDB) = Future {
    ts match {
      case Nil => ts
      case ts => coll.insert(ts.map(entityToDbo): _*); ts
    }
  }

  /**
    * Save the object and either inserts or updates it.
    *
    * @param t the updated version of the object.
    */
  def save(t: T)(implicit ec: ExecutionContext, db: MongoDB) = Future {
    coll.save(entityToDbo(t))
  }

  /**
    * Updates the object with the specified ID to the new version.
    *
    * @param id the ID of the object to update.
    * @param t  the updated version of the object.
    */
  def update(id: ID, t: T)(implicit ec: ExecutionContext, db: MongoDB) = Future {
    coll.update(identityObject(id), entityToDbo(t))
  }


  /**
    * Updates all the values in the objects matching the query with the new ones passed
    *
    * @param params  a map that contains the fields with the values to match against.
    * @param updated a map that contains the fields with the updated values.
    */
  def updateAll(params: Map[String, _], updated: Map[String, _])(implicit ec: ExecutionContext, db: MongoDB) = Future {
    coll.update(params, Map("$set" -> updated), multi = true)
  }

  /**
    * Updates all the objects matching the query with the new ones passed through a function
    *
    * @param params a map that contains the fields with the values to match against.
    * @param f      a function that takes one object and transforms it into another.
    */
  def updateAll(params: Map[String, _], f: T => T)(implicit ec: ExecutionContext, db: MongoDB): Future[List[WriteResult]] =
    query(params).map(_.map(t => coll.save(entityToDbo(f(t)))))

  /**
    * Removes the object from the database.
    *
    * @param t the object to remove.
    */
  def remove(t: T)(implicit ec: ExecutionContext, db: MongoDB) = Future {
    coll.remove(entityToDbo(t))
  }

  /**
    * Removes the object from the database as specified by the ID.
    *
    * @param id the ID of the object to remove.
    */
  def removeById(id: ID)(implicit ec: ExecutionContext, db: MongoDB) = Future {
    coll.remove(identityObject(id))
  }

  /**
    * Removes all objects matching the query.
    *
    * @param params a map of object parameters
    * @return the success of the operation.
    */
  def removeAll(params: Map[String, String])(implicit ec: ExecutionContext, db: MongoDB) = Future {
    val q = DBObject()
    q.putAll(params)
    coll.remove(q)
  }

  /**
    * Looks up the specified object from the database.
    *
    * @param id the ID of the object to lookup.
    * @return an option with the matching object, None if no one exists.
    */
  def findOne(id: ID)(implicit ec: ExecutionContext, db: MongoDB): Future[Option[T]] = Future {
    coll.findOne(identityObject(id)).map(dboToEntity)
  }


  /**
    * Looks up all objects of this type.
    *
    * @return a list of all objects of managed by this DAO.
    */
  def findAll()(implicit ec: ExecutionContext, db: MongoDB): Future[List[T]] =
    findAllStream.map(_.toList)

  /**
    * Looks up all objects of this type.
    *
    * @return an iterator  of all objects of managed by this DAO.
    */
  protected def findAllStream()(implicit ec: ExecutionContext, db: MongoDB): Future[Iterator[T]] = Future {
    coll.find().map(dboToEntity)
  }

  /**
    * Performs a query on the database and returns a list of all matching elements.
    *
    * @param params a map that contains the fields with the values to match against.
    * @return a list of all matching elements.
    */
  def query(params: Map[String, _])(implicit ec: ExecutionContext, db: MongoDB): Future[List[T]] =
    queryStream(params).map(_.toList)


  /**
    * Performs a query on the database and returns an iterator of all matching elements.
    *
    * @param params a map that contains the fields with the values to match against.
    * @return an iterator of all matching elements.
    */
  protected def queryStream(params: Map[String, _])(implicit ec: ExecutionContext, db: MongoDB): Future[Iterator[T]] = Future {
    val q = DBObject()
    q.putAll(params)
    coll.find(q).map(dboToEntity)
  }

  /**
    * Performs a query on the database and returns an Option of the first matching element.
    *
    * @param params a map that contains the fields with the values to match against.
    * @return an option with the first matching element, None if nothing was found.
    */
  def queryOne(params: Map[String, _])(implicit ec: ExecutionContext, db: MongoDB): Future[Option[T]] = Future {
    val q = DBObject()
    q.putAll(params)
    coll.findOne(q).map(dboToEntity)
  }

  /**
    * Performs a count query on the database and returns the total number of documents in the collection.
    *
    * @return the total number of documents in the collection.
    */
  def countAll()(implicit ec: ExecutionContext, db: MongoDB): Future[Int] = Future {
    coll.count()
  }

  /**
    * Performs a count query on the database and returns the number of documents in the collection that
    * match the queyr filters.
    *
    * @param params a map that contains the fields with the values to match against.
    * @return the number of matching documents in the collection.
    */
  def count(params: Map[String, _])(implicit ec: ExecutionContext, db: MongoDB): Future[Int] = Future {
    coll.count(params)
  }
}
