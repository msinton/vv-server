package com.consideredgames.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.consideredgames.server.db.Indexes

import scala.util.{Failure, Success}

/**
  * Created by matt on 22/03/17.
  */
object Boot extends App {

  implicit val system = ActorSystem("vv-server")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val config = system.settings.config
  val host = config.getString("app.host")
  val port = config.getInt("app.port")

  Indexes.initialise().onFailure {
    case e =>
      println(s"Indexes failed to initialise: ${e.getMessage}")
      system.terminate()
  }

  val server = new GameService()

  val binding = Http().bindAndHandle(server.route, host, port)
  binding.onComplete {
    case Success(serverBinding) =>
      val localAddress = serverBinding.localAddress
      println(s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
    case Failure(e) =>
      println(s"Binding failed with ${e.getMessage}")
      system.terminate()
  }
}
