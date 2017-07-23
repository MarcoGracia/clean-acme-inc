package util

import org.specs2.mutable.BeforeAfter
import com.mongodb.casbah.Imports._
import java.util.UUID

import util.FutureTestSupport

import scala.concurrent.util

// Conviniency class to add mongodb support to tests

trait MongoContext extends BeforeAfter {
  val contextId = UUID.randomUUID().toString

  val mongoHost = "localhost"
  val mongoPort = 27018
  val mongoUri = MongoClientURI("mongodb://" + mongoHost + ":" + mongoPort)
  val testDbName = MongoContext.DB_NAME_PREFIX + (Math.random() * 100000).toInt

  private var _mongoClient: MongoClient = _

  def mongoClient = _mongoClient

  def getDb() = _mongoClient(testDbName)

  override def before() {
    _mongoClient = MongoClient(mongoUri)
  }

  override def after() {
    if (_mongoClient != null) {
      _mongoClient.dropDatabase(testDbName)
      _mongoClient.close()
    }
  }
}

object MongoContext {
  val DB_NAME_PREFIX = "unittest"
}