package com.consideredgames.server.db

import com.mongodb.MongoWriteException
import org.mongodb.scala.Completed
import org.mongodb.scala.model.Filters._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by matt on 11/05/17.
  */
class UserDaoTest extends FunSuite with BeforeAndAfterEach with ScalaFutures {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(8, Seconds), interval = Span(10, Millis))

  override def beforeEach(): Unit = {
    UserDao.indexes.futureValue
  }

  override def afterEach(): Unit = {
    val del = UserDao.users.deleteMany(equal("username", "Jimmy")).toFuture().futureValue
    println(del)
  }

  val username = "Jimmy"
  val email = "jim@m.com"
  val hash = "hashshs"

  test("testInsertUser") {
    val insert = UserDao.insert(User(username, email, hash)).futureValue
    assert(insert.isInstanceOf[Completed])
  }

  test("testInsertUser - unique index on username") {

    val t = for {
      _ <- UserDao.insert(username, email, hash)
      x <- UserDao.insert(username, "other-email", hash)
    } yield x

    val result = t.failed.futureValue

    assert(result.isInstanceOf[MongoWriteException])
    assert(result.getMessage.startsWith("E11000 duplicate key error collection: vv.users index: username_1 dup key"))
  }

  test("testInsertUser - unique index on email") {

    val t = for {
      _ <- UserDao.insert(username, email, hash)
      x <- UserDao.insert("other-username", email, hash)
    } yield x

    val result = t.failed.futureValue
    assert(result.isInstanceOf[MongoWriteException])
    assert(result.getMessage.startsWith("E11000 duplicate key error collection: vv.users index: email_1 dup key"))
  }

  test("testGetUser") {

    UserDao.insert(User(username, email, hash)).futureValue
    val result = UserDao.get("Jimmy", "jim@m.com").futureValue
    assert(result.email == email && result.hash == hash && result.username == username)
  }

  test("getUsersByUsernames") {

    val userToInsert = User(username, email, hash)
    UserDao.insert(userToInsert).futureValue

    val result = UserDao.getByUsernames(List(username, "bob")).futureValue

    assert(result.size == 1)
    assert(result.head == userToInsert)
  }
}


