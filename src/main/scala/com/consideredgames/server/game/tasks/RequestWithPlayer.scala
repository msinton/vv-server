package com.consideredgames.server.game.tasks

import akka.actor.ActorRef
import com.consideredgames.message.Messages.Request

/**
  * Created by matt on 14/07/17.
  */
case class RequestWithPlayer(request: Request, username: String, playerWorker: ActorRef)
