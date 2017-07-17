package com.consideredgames.server.routes.sessions

import com.consideredgames.server.db.SessionDao

import scala.concurrent.{ExecutionContext, Future}


/**
  * Created by matt on 13/07/17.
  */
package object SessionUtils {

  private val preparedSessionTimeout = 5 * 1000

  def prepareSession(username: String, ip: String): String = SessionDao.insert(username, ip)

  def hasValidSession(username: String, sessionId: String, ip: String)
                     (implicit ec: ExecutionContext): Future[Boolean] =
    SessionDao.getRecent(sessionId, username, ip, preparedSessionTimeout)
      .map(_ != null)

}
