package com.consideredgames.server.db

import java.util.Date

import com.consideredgames.server.db.Mongo._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.json4s.DefaultFormats
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.{BsonDateTime, ObjectId}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.IndexModel
import org.mongodb.scala.model.Indexes.{ascending, _}

import scala.concurrent.Future

/**
  * Created by matt on 11/07/17.
  */
object SessionDao {

  private val codecRegistry = fromRegistries(fromProviders(classOf[Session]), DEFAULT_CODEC_REGISTRY)

  val sessions: MongoCollection[Session] = mongoClient.getDatabase("vv")
    .withCodecRegistry(codecRegistry)
    .getCollection("sessions")

  val indexes: Future[String] = sessions.createIndexes(List(
    IndexModel(ascending("username")),
    IndexModel(descending("createdAt"))
  )).toFuture()

  implicit val formats = DefaultFormats

  def insert(username: String, ip: String): String = {
    val session = Session(username, ip)
    sessions.insertOne(session).toFuture()
    session._id.toString
  }

  def getRecent(id: String, username: String, ip: String, withinMillis: Long): Future[Session] =
    sessions.find(
      and(
        equal("_id", new ObjectId(id)),
        equal("username", username),
        equal("ip", ip),
        gte("createdAt", BsonDateTime(new Date().getTime - withinMillis))
      )
    ).first().toFuture()

}

case class Session(_id: ObjectId, username: String, ip: String, createdAt: BsonDateTime)

object Session {
  def apply(username: String, ip: String): Session =
    Session(new ObjectId(), username, ip, BsonDateTime(new Date()))
}