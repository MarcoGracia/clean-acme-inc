package acme.inc.mongo

import com.mongodb.casbah.Imports._
import java.lang.{Boolean => JBoolean}
import java.lang.{Double => JDouble}
import java.lang.{Float => JFloat}
import java.lang.{Integer => JInteger}
import java.lang.{Long => JLong}
import spray.json._


/**
  * A set of utility methods and implicits that convert spray json objects into Mongo objects.
  */
object MongoJsConverter {
  implicit class PimpedJsValue(jsValue: JsValue) {
    def toDbObject: DBObject = jsValue match {
      case jsObject: JsObject => jsObjectToDbObject0(jsObject, true)
      case otherValue => throw new IllegalArgumentException("Cannot convert simple value '" + otherValue + "' to object")
    }

    private def jsObjectToDbObject0(jsObject: JsObject, root: Boolean): DBObject = jsObject.fields.map {
      case (name, value) => (name, value) match {
        case ("id", value) if root => "_id" -> jsValueToDbObject0(value)
        case (name, value) => name -> jsValueToDbObject0(value)
      }
    }

    private def jsValueToDbObject0(jsValue: JsValue): Any = jsValue match {
      case JsNumber(decimal) => decimal.toDouble
      case JsString(str) => str
      case JsBoolean(bool) => bool
      case JsArray(array) => array.map(e => jsValueToDbObject0(e))
      case jsObj: JsObject => jsObjectToDbObject0(jsObj, false)
      case o => throw new IllegalArgumentException("Could not parse type " + o.getClass)
    }
  }

  implicit class PimpedDbObject(dbObject: DBObject) {
    private def toJsObject(dbObject: DBObject, root: Boolean): JsObject = new JsObject(dbObject.mapValues(_ match {
      case str: String => JsString(str)
      case long: JLong => JsNumber(long)
      case int: JInteger => JsNumber(int)
      case double: JDouble => JsNumber(double)
      case float: JFloat => JsNumber(float.toDouble)
      case bool: JBoolean => JsBoolean(bool)
      case list: BasicDBList => JsArray(list.map (item => item match {
        case str: String => JsString(str)
        case long: JLong => JsNumber(long)
        case int: JInteger => JsNumber(int)
        case double: JDouble => JsNumber(double)
        case float: JFloat => JsNumber(float.toDouble)
        case bool: JBoolean => JsBoolean(bool)
        case obj: DBObject => toJsObject(obj, false)
        case o => throw new IllegalArgumentException("Cannot parse type " + o.getClass)
      }) : _*)
      case obj: DBObject => toJsObject(obj, false)
      case null => JsNull
      case o => throw new IllegalArgumentException("Cannot parse type " + o.getClass)
    }).map { case (key, value) => key match {
      case "_id" if root => "id" -> value
      case _ => key -> value
    }
    }.toMap)

    def toJsObject: JsObject = toJsObject(dbObject, true)
  }

}