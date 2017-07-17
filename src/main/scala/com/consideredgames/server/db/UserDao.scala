package com.consideredgames.server.db

import com.consideredgames.server.db.Mongo._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.json4s.DefaultFormats
import org.mongodb.scala.{Completed, MongoCollection}
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.{IndexModel, IndexOptions}

import scala.concurrent.Future

/**
  * Created by matt on 11/05/17.
  */
object UserDao {

  private val codecRegistry = fromRegistries(fromProviders(classOf[User]), DEFAULT_CODEC_REGISTRY)

  val users: MongoCollection[User] = mongoClient.getDatabase("vv")
    .withCodecRegistry(codecRegistry)
    .getCollection("users")

  val indexes = users.createIndexes(List(
    IndexModel(ascending("username"), IndexOptions().unique(true)),
    IndexModel(ascending("email"), IndexOptions().unique(true))
  )).toFuture()

  implicit val formats = DefaultFormats

  def insert(username: String, email: String, hash: String): Future[Completed] =
    users.insertOne(User(username, email, hash)).toFuture()

  def insert(user: User) = users.insertOne(user).toFuture()

  def get(username: String, email: String) =
    users.find(and(equal("username", username), equal("email", email)))
      .first().toFuture()

  def getByUsernames(usernames: List[String]) = {
    users.find[User](in[String]("username", usernames:_*)).toFuture()
  }
}

case class User(_id: ObjectId, username: String, email: String, hash: String)

object User {
  def apply(username: String, email: String, hash: String): User =
    User(new ObjectId(), username, email, hash)
}