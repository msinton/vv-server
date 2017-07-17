package com.consideredgames.server.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, OverflowStrategy}
import com.consideredgames.message.MessageMapper
import com.consideredgames.message.Messages.{Message => VVMessage}
import com.consideredgames.server.events.{Event, SessionEvent, SessionLogout, SessionStart}

/**
  * Created by matt on 11/07/17.
  */
class WebsocketFlow(sessionWorker: ActorRef) {

  private val playerActorSource = Source.actorRef[VVMessage](bufferSize = 5, OverflowStrategy.fail)

  def flow(username: String, sessionId: String, ip: String): Flow[Message, Message, Any] =
    Flow.fromGraph(GraphDSL.create(playerActorSource) { implicit builder =>
      playerActor =>

        import GraphDSL.Implicits._

        val materialization = builder.materializedValue.map(actorRef =>
          SessionStart(username, sessionId, ip, actorRef))

        val merge = builder.add(Merge[Event](2))

        val messagesToEventFlow = builder.add(Flow[Message].collect {
          case TextMessage.Strict(msg) =>
            println(s"got message ----- $msg")
            SessionEvent(MessageMapper.deJsonify(msg), username)
        })

        val VVMessagesToMessagesFlow = builder.add(Flow[VVMessage].map(message =>
          TextMessage(MessageMapper.toJson(message))))

        val sessionActorSink = Sink.actorRef[Event](sessionWorker, SessionLogout(username))

        materialization ~> merge ~> sessionActorSink
        messagesToEventFlow ~> merge

        playerActor ~> VVMessagesToMessagesFlow

        FlowShape(messagesToEventFlow.in, VVMessagesToMessagesFlow.out)
    })
}
