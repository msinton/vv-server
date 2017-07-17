package com.consideredgames.server.game

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.consideredgames.game.model.player.PlayerColours.PlayerColour
import com.consideredgames.message.Messages._
import com.consideredgames.server.game.tasks._

import scala.concurrent.ExecutionContext

/**
  * Created by matt on 14/07/17.
  */
class LobbyActor(implicit val actorSystem: ActorSystem,
                 implicit val actorMaterializer: ActorMaterializer,
                 implicit val ec: ExecutionContext) extends Actor {

  private val stagedGames = collection.mutable.LinkedHashMap[String, ActorRef]()
  private val privateStagedGames = collection.mutable.LinkedHashMap[String, ActorRef]()
  private val gamesWorker = actorSystem.actorOf(Props[GamesActor], "games-worker")

  private def addToStaging(newGameOptions: Option[NewGameOptions], gameId: String, gameWorker: ActorRef) = {
    val stagingCollection = if (newGameOptions.exists(_.privateGame))
      privateStagedGames else stagedGames
    stagingCollection += (gameId -> gameWorker)
  }

  private def createGameWorker(newGameRequest: NewGameRequest) = {
    val gameId = UUID.randomUUID().toString
    val gameWorker = actorSystem.actorOf(Props(new GameActor(gameId, newGameRequest)), s"game-worker-$gameId")
    (gameWorker, gameId)
  }

  private def stageNewGame(request: NewGameRequest, username: String, playerWorker: ActorRef) = {
    println(s"new-game request for $username, $request")
    val (gameWorker, gameId) = createGameWorker(request)
    addToStaging(request.newGameOptions, gameId, gameWorker)
    (gameId, gameWorker)
  }

  private def switchToActive(gameId: String, isPrivate: Boolean) = {
    val games = if (isPrivate) privateStagedGames else stagedGames
    games.get(gameId).foreach { game =>
      gamesWorker ! StartGame(gameId, game, isPrivate)
    }
  }

  private def removeGame(gameId: String, isPrivate: Boolean) = {
    val games = if (isPrivate) privateStagedGames else stagedGames
    games -= gameId
  }

  private def handleRequestWithPlayer(request: Request, username: String, playerWorker: ActorRef) = {
    request match {
      case newGameRequest: NewGameRequest =>
        val (gameId, gameWorker) = stageNewGame(newGameRequest, username, playerWorker)
        gameWorker ! AddPlayer(username, playerWorker, newGameRequest.myColour)
        playerWorker ! NewGameResponse(gameId)

      case Join(colour, Some(gameId)) =>
        println(s"join request for $username, $colour, $gameId")
        privateStagedGames.get(gameId).fold {
          playerWorker ! JoinResponseFailure(List(s"No game exists with id: $gameId"))
        } { gameWorker =>
          gameWorker ! AddPlayer(username, playerWorker, colour)
        }

      case Join(colour, _) =>
        println(s"join request for $username, $colour")
        val (gameId, gameWorker) = stagedGames.headOption.getOrElse(
          stageNewGame(NewGameRequest(myColour=colour), username, playerWorker))
        gameWorker ! AddPlayer(username, playerWorker, colour)
    }
  }

  override def receive: Receive = {

    case RequestWithPlayer(request, username, playerWorker) =>
      handleRequestWithPlayer(request, username, playerWorker)

    case GameFull(gameId, isPrivate) => switchToActive(gameId, isPrivate)

    case GameIsActive(gameId, isPrivate) => removeGame(gameId, isPrivate)

  }


}
