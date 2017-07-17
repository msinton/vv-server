package com.consideredgames.server.events

import akka.actor.ActorRef

/**
  * Created by matt on 13/07/17.
  */
case class SessionStart(username: String, sessionId: String, ip: String, actorRef: ActorRef) extends Event
