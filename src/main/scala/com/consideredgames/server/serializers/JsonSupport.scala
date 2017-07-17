package com.consideredgames.server.serializers

import com.consideredgames.message.MessageMapper
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{Formats, native}

/**
  * Created by matt on 22/05/17.
  */
trait JsonSupport extends Json4sSupport {

  implicit val serialization = native.Serialization

  implicit def json4sFormats: Formats = MessageMapper.formats
}
