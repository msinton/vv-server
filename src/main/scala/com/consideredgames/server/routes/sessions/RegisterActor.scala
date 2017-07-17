package com.consideredgames.server.routes.sessions

import akka.actor.Actor
import akka.pattern.pipe
import com.consideredgames.message.Messages._
import com.consideredgames.security.EncryptionUtils
import com.consideredgames.server.db.UserDao
import com.consideredgames.server.routes.tasks.MessageWithIp
import com.mongodb.MongoWriteException

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random


/**
  * Created by matt on 13/05/17.
  */
class RegisterActor(implicit val executionContext: ExecutionContext) extends Actor {

  private val usernameMaxLength = 20
  private val usernameTaken = "E11000 duplicate key error collection: vv.users index: username_1"
  private val emailTaken = "E11000 duplicate key error collection: vv.users index: email_1"
  private val emailTakenMessage = "email address is already in use"

  private type ErrorHandlerType = PartialFunction[Throwable, Future[Message]]

  private val handleEmailTaken: ErrorHandlerType = {
    case e: MongoWriteException if e.getMessage.startsWith(emailTaken) =>
      Future(RegisterResponseInvalid(List(emailTakenMessage)))
  }

  private def handleUsernameTaken(username: String): ErrorHandlerType = {
    case e: MongoWriteException if e.getMessage.startsWith(usernameTaken) =>
      getAlternativeUsernames(username) map RegisterResponseUsernameUnavailable
  }

  private val handleFailureDefault: ErrorHandlerType = {
    case e =>
      println("default handler", e, e.getMessage)
      e.printStackTrace()
      Future(RegisterResponseInvalid(List("try again later")))
  }

  private def createUserAndResponse(username: String, email: String, passwordHash: String, ip: String) = {
    val recoverStrategy = handleEmailTaken orElse handleUsernameTaken(username) orElse handleFailureDefault
    val hashedHash = EncryptionUtils.encrypt(passwordHash)

    (UserDao.insert(username, email.toLowerCase, hashedHash) map { _ =>
      SessionUtils.prepareSession(username, ip)} map (RegisterResponseSuccess(username, _))
    ) recoverWith recoverStrategy
  }

  private def getAlternativeUsernames(username: String): Future[List[String]] = {

    def generatePossibleAlternatives = ((1 to 10) map { _ =>
      val randomSuffix = "_" + Random.nextInt(5000)
      val newUsernameLength = Math.min(usernameMaxLength - randomSuffix.length, username.length)
      username.substring(0, newUsernameLength) + randomSuffix
    }).toList

    def take3ViableAlternatives(alternatives: List[String]) =
    UserDao.getByUsernames(alternatives) map { users =>
      val usernamesInUse = users.map(_.username)
      alternatives.filterNot(usernamesInUse.contains).take(3)
    }

    take3ViableAlternatives(generatePossibleAlternatives)
  }

  override def receive: Receive = {
    case MessageWithIp(Register(username, _, _), _) if username.length > usernameMaxLength =>
      sender() ! RegisterResponseInvalid(List("username too long"))

    case MessageWithIp(Register(username, passwordHash, email), ip) =>
      createUserAndResponse(username, email, passwordHash, ip) pipeTo sender()
  }
}

