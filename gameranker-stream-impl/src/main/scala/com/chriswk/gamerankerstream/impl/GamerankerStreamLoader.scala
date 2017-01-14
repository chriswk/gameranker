package com.chriswk.gamerankerstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import play.api.libs.ws.ahc.AhcWSComponents
import com.chriswk.gamerankerstream.api.GamerankerStreamService
import com.chriswk.gameranker.api.GamerankerService
import com.softwaremill.macwire._

class GamerankerStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new GamerankerStreamApplication(context) {
      override def serviceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new GamerankerStreamApplication(context) with LagomDevModeComponents
}

abstract class GamerankerStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the services that this server provides
  override lazy val lagomServer = LagomServer.forServices(
    bindService[GamerankerStreamService].to(wire[GamerankerStreamServiceImpl])
  )

  // Bind the GamerankerService client
  lazy val gamerankerService = serviceClient.implement[GamerankerService]
}
