package com.consideredgames.server.events
import com.consideredgames.message.Messages.{Message => VVMessage}

/**
  * Created by matt on 13/07/17.
  */
case class SessionEvent(vvMessage: VVMessage, sessionId: String) extends Event
