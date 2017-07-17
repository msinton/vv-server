package com.consideredgames.server.game.tasks

import akka.actor.ActorRef
import com.consideredgames.game.model.player.PlayerColours.PlayerColour

/**
  * Created by matt on 14/07/17.
  */
case class AddPlayer(username: String, playerWorker: ActorRef, colour: PlayerColour)
