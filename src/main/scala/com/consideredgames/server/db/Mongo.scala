package com.consideredgames.server.db

import org.mongodb.scala.MongoClient

/**
  * Created by matt on 22/05/17.
  */
object Mongo {
  val mongoClient: MongoClient = MongoClient("mongodb://localhost")
}
