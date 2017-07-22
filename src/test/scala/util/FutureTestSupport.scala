package util

import scala.concurrent._
import scala.concurrent.duration._

trait FutureTestSupport {
  import scala.language.implicitConversions

  protected[this] val awaitTimeout = FutureTestSupport.DEFAULT_TIMEOUT

  implicit def pimpFuture[T](future: Future[T]) = new PimpedFuture(future)

  class PimpedFuture[T](future: Future[T]) {
    @deprecated(message = "in favor of awaitResult because of conflicting 2.11 changes in specs2", since = "1.0.3")
    def await = Await.result(future, awaitTimeout)
    def awaitResult = Await.result(future, awaitTimeout)
  }
}

object FutureTestSupport {
  val DEFAULT_TIMEOUT = 30.seconds
}