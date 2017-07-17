package com.consideredgames.server.routes.tasks

import com.consideredgames.message.Messages.Message

/**
  * Created by matt on 13/07/17.
  */
case class MessageWithIp(m: Message, ip: String)
