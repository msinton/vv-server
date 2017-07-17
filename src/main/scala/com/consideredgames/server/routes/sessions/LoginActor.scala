package com.consideredgames.server.routes.sessions

import akka.actor.Actor
import akka.pattern.pipe
import com.consideredgames.message.Messages.{Login, LoginResponseInvalid, LoginResponseSuccess}
import com.consideredgames.security.EncryptionUtils
import com.consideredgames.server.db.{User, UserDao}
import com.consideredgames.server.routes.tasks.MessageWithIp

import scala.concurrent.ExecutionContext

/**
  * Created by matt on 13/07/17.
  */
class LoginActor(implicit val executionContext: ExecutionContext) extends Actor {

  private def handleIsAuthenticated(username: String, ip: String) = {
    val sessionId = SessionUtils.prepareSession(username, ip)
    LoginResponseSuccess(username, sessionId)
  }

  private def authenticateUserAndResponse(username: String, passwordHash: String, email: String, ip: String) = {
    UserDao.get(username, email.toLowerCase) collect {
      case User(_, _, _, h) if EncryptionUtils.isPasswordMatch(passwordHash, h) => handleIsAuthenticated(username, ip)
      case _: User => LoginResponseInvalid(List("password incorrect"))
      case _ => LoginResponseInvalid(List("user not found"))
    }
  }

  override def receive: Receive = {

    case MessageWithIp(Login(username, passwordHash, email), ip) =>
      authenticateUserAndResponse(username, passwordHash, email, ip) pipeTo sender()

  }
}
