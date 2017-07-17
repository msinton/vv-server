package com.consideredgames.server.game

import akka.actor.{Actor, ActorRef}
import com.consideredgames.server.game.tasks.{GameIsActive, StartGame}

/**
  * Created by matt on 15/07/17.
  */
class GamesActor extends Actor {

  private val games = collection.mutable.LinkedHashMap[String, ActorRef]()

  override def receive: Receive = {
    case r@StartGame(gameId, gameWorker, isPrivate) =>
      games += (gameId -> gameWorker)
      sender() ! GameIsActive(gameId, isPrivate)
      gameWorker ! r


  }
}
