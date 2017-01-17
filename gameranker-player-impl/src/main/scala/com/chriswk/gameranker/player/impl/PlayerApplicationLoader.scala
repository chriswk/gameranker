package com.chriswk.gameranker.player.impl

import com.chriswk.gameranker.player.api.PlayerService
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import play.api.libs.ws.ahc.AhcWSComponents
import com.softwaremill.macwire._

abstract class PlayerApplication(context: LagomApplicationContext) extends LagomApplication(context)
  with AhcWSComponents
  with CassandraPersistenceComponents {

  override lazy val lagomServer = LagomServer.forServices(
    bindService[PlayerService].to(wire[PlayerServiceImpl])
  )

  persistentEntityRegistry.register(wire[PlayerEntity])
}

class PlayerApplicationLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext) = new PlayerApplication(context) {
    override def serviceLocator = NoServiceLocator
  }

  override def loadDevMode(context: LagomApplicationContext) =
    new PlayerApplication(context) with LagomDevModeComponents
}
