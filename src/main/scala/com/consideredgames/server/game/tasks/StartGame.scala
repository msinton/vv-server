package com.consideredgames.server.game.tasks

import akka.actor.ActorRef

/**
  * Created by matt on 15/07/17.
  */
case class StartGame(gameId: String, gameWorker: ActorRef, isPrivate: Boolean)
