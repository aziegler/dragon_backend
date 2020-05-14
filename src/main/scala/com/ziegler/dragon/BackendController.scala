package com.ziegler.dragon

import com.ziegler.dragon.data.DatabaseSession
import com.ziegler.dragon.models.{Dice, DragonDb, Theme}
import org.bson.types.ObjectId
import org.json4s.JsonAST.{JArray, JField, JInt, JLong, JObject, JString}
import org.scalatra._
import org.json4s.{CustomSerializer, DefaultFormats, FieldSerializer, Formats, JValue, JsonAST, MappingException, Serializer, TypeInfo}
import org.mongodb.scala.MongoCollection
import org.scalatra.json._
import org.scalatra.atmosphere._
import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId


import scala.concurrent.ExecutionContext.Implicits.global

class BackendController(themesData : MongoCollection[Theme], diceData : MongoCollection[Dice]) extends ScalatraServlet with JacksonJsonSupport with CorsSupport with AtmosphereSupport {

  protected implicit lazy val jsonFormats: Formats = DefaultFormats + new ObjectIdSerializer

  class ObjectIdSerializer extends Serializer[ObjectId] {
    private[this] val ObjectIdClass = classOf[ObjectId]
    def objectIdAsJValue(oid: ObjectId): JValue = JObject(JField("id", JString(oid.toString)) :: Nil)
    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), ObjectId] = {
      case (TypeInfo(ObjectIdClass, _), json) => json match {
        case JObject(JField("id", JString(s)) :: Nil) if (ObjectId.isValid(s)) =>
          new ObjectId(s)
        case x => throw new MappingException(s"Can't convert $x to ObjectId")
      }
    }

    def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
      case x: ObjectId => objectIdAsJValue(x)
    }
  }





  before() {
    contentType = formats("json")
  }

  get("/") {
    val themes = DragonDb.Theme.list(themesData)
    themes.toList
  }

  get("/dices") {
    val dices = DragonDb.Dice.list(diceData)
    dices.toList
  }

  atmosphere("/broadcast") {
    new AtmosphereClient {
      def receive = {
        case Connected =>
        case Disconnected(disconnector, Some(error)) =>
        case Error(Some(error)) =>
        case TextMessage(text) => broadcast(text,Others)
        case JsonMessage(json) => broadcast(json, Others)
      }
    }
  }



  options("/*"){
    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
    response.setHeader("Allow", request.getHeader("GET,POST,OPTIONS"))
  }

  post ("/dice") {
    val dice = parsedBody.extract[Dice]
    DragonDb.Dice.createOrUpdate(dice, diceData)
    val dices = DragonDb.Dice.list(diceData)
    dices.toList
  }



  post("/theme") {
    val theme = parsedBody.extract[Theme]
    DragonDb.Theme.createOrUpdate(theme,themesData)
    DragonDb.Theme.list(themesData)
  }

  delete ("/dice") {
    val dice = parsedBody.extract[Dice]
    DragonDb.Dice.delete(dice, diceData)
    val dices = DragonDb.Dice.list(diceData)
    dices.toList

  }

  delete("/theme") {
    val theme = parsedBody.extract[Theme]
    DragonDb.Theme.delete(theme, themesData)
    val themes = DragonDb.Theme.list(themesData)
    themes
  }



}
