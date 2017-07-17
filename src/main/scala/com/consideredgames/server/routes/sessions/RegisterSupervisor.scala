package com.consideredgames.server.routes.sessions

import akka.actor.{Actor, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.consideredgames.server.routes.tasks.MessageWithIp

import scala.concurrent.ExecutionContext

/**
  * Created by matt on 13/07/17.
  */
class RegisterSupervisor(implicit val executionContext: ExecutionContext) extends Actor {

  var router: Router = {
    val routees = Vector.fill(10) {
      val worker = context.actorOf(Props(new RegisterActor()))
      context watch worker
      ActorRefRoutee(worker)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def receive: Receive = {
    case m: MessageWithIp => router.route(m, sender())

    case Terminated(a) =>
      println("TERMINATED: register actor terminated!")
      router = router.removeRoutee(a)
      val worker = context.actorOf(Props(new RegisterActor()))
      context watch worker
      router = router.addRoutee(worker)
  }
}
