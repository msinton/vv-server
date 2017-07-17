package com.consideredgames.server.db

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by matt on 11/07/17.
  */
object Indexes {

  def initialise()(implicit ec: ExecutionContext): Future[Any] = {
    for {
      _ <- UserDao.indexes
      f <- SessionDao.indexes
    } yield f
  }
}
