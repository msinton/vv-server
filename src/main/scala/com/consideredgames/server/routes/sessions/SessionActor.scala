package com.consideredgames.server.routes.sessions

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.stream.ActorMaterializer
import com.consideredgames.message.Messages._
import com.consideredgames.server.events.{SessionEvent, SessionLogout, SessionStart}
import com.consideredgames.server.game.tasks.RequestWithPlayer

import scala.concurrent.ExecutionContext

/**
  * Created by matt on 30/04/17.
  */
class SessionActor(lobbyWorker: ActorRef)(implicit val ec: ExecutionContext) extends Actor {

  private val active = collection.mutable.LinkedHashMap[String, ActorRef]()

  private def getActive(username: String): Option[ActorRef] = active.get(username)

  private def addToActive(username: String, playerWorker: ActorRef): Unit =
    active += (username -> playerWorker)

  private def removeFromActive(username: String): Unit = active -= username

  private def cleanupDeadSession(username: String) = {
    getActive(username).foreach { oldPlayerWorker =>
      oldPlayerWorker ! PoisonPill
      removeFromActive(username)
    }
  }

  private def checkSessionActiveAndForwardRequest(worker: ActorRef, request: Request, username: String) = {
    getActive(username).foreach { playerWorker =>
      worker.forward(RequestWithPlayer(request, username, playerWorker))
    }
  }

  private def handleRequest(request: Request, username: String) = {
    request match {
      case r: NewGameRequest => checkSessionActiveAndForwardRequest(lobbyWorker, r, username)
      case r: Join => checkSessionActiveAndForwardRequest(lobbyWorker, r, username)
      case Logout() => cleanupDeadSession(username)
    }
  }

  override def receive: Receive = {

    case SessionStart(username, sessionId, ip, playerWorker) =>
      println(s"Got session start $username $ip")
      SessionUtils.hasValidSession(username, sessionId, ip) collect {
        case true =>
          cleanupDeadSession(username)
          addToActive(username, playerWorker)
          playerWorker ! SessionStarted()
        case false =>
          playerWorker ! ForceLogout(username)
          playerWorker ! PoisonPill
      }

    case SessionEvent(vvMessage: Request, username) =>
      println(s"forwarding session event: $username, $vvMessage")
      handleRequest(vvMessage, username)

    case SessionEvent(vvMessage, username) =>
      println(s"got non-request: $username, $vvMessage")

    case SessionLogout(username) => removeFromActive(username)
  }

}

