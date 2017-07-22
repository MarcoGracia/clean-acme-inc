package acme.inc.mongo

import com.mongodb.casbah.Imports._

/**
  * A standard wrapper around a Mongo connection to manage the state. Can be subclasses and should be used
  * to provide a singleton within an application.
  */
trait MongoConnection {
  protected[this] def mongoUri: String

  private val mongoClientUri = MongoClientURI(mongoUri)
  private val mongoClient = MongoClient(mongoClientUri)

  /**
    * Creates an instance of a database that is based on the URI of this connection.
    *
    * @return a db instance for this connections' URI.
    */
  def getDefaultDb() = {
    getDb(mongoClientUri.database.get)
  }

  /**
    * Returns an instance of a database specified by the name.
    *
    * @param dbName the name of the database.
    *
    * @return an instance of the database.
    */
  def getDb(dbName: String) = {
    mongoClient(dbName)
  }

  def shutdown() {
    mongoClient.close()
  }
}

