package com.consideredgames.server.game

import akka.actor.{Actor, ActorRef}
import com.consideredgames.game.model.player.{PlaceholderPlayer, PlayerColours}
import com.consideredgames.game.model.player.PlayerColours.PlayerColour
import com.consideredgames.message.Messages.{JoinResponseSuccess, NewGameOptions, NewGameReady, NewGameRequest}
import com.consideredgames.server.game.tasks.{AddPlayer, GameFull, StartGame}

import scala.util.Random

case class State(players: Map[String, PlaceholderPlayer])

/**
  * Created by matt on 14/07/17.
  */
class GameActor(gameId: String, request: NewGameRequest) extends Actor {

  private val isPrivate = request.newGameOptions.exists(_.privateGame)
  private val playerWorkers = collection.mutable.LinkedHashMap[String, ActorRef]()
  private val playerColours = collection.mutable.LinkedHashMap[String, PlayerColour]()
  private val random = new Random()
  private var state: State = _


  private def resolveColour(colour: PlayerColour) = {
    val takenColours = playerColours.values.toList
    if (takenColours.contains(colour))
      PlayerColours.playerColoursSet.diff(takenColours.toSet).head
    else
      colour
  }

  private def addPlayer(username: String, playerWorker: ActorRef, colour: PlayerColour) = {
    playerWorkers += (username -> playerWorker)
    playerColours += (username -> resolveColour(colour))
    playerWorker ! JoinResponseSuccess(gameId)
  }

  override def receive: Receive = {

    case AddPlayer(username, playerWorker, colour) =>
      println(s"add player $username")

      if (playerWorkers.size < request.numberOfPlayers)
        addPlayer(username, playerWorker, colour)

      if (playerWorkers.size == request.numberOfPlayers)
        sender() ! GameFull(gameId, isPrivate)

    case _: StartGame =>
      val playerPlaceholders = playerColours.toList.map {case (name, colour) => PlaceholderPlayer(name, colour)}
      val seed = request.seed.getOrElse(random.nextLong())
      val options = request.newGameOptions.getOrElse(NewGameOptions())
      playerWorkers.values.foreach { playerWorker =>
        playerWorker ! NewGameReady(gameId, playerPlaceholders, seed, options)}

  }
}
