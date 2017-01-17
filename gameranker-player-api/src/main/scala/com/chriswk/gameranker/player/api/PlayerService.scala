package com.chriswk.gameranker.player.api

import java.util.UUID

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

trait PlayerService extends Service {
  def createPlayer: ServiceCall[CreatePlayer, Player]

  def getPlayer(playerId: UUID): ServiceCall[NotUsed, Player]

  def getPlayers: ServiceCall[NotUsed, Seq[Player]]

  override def descriptor = {
    import Service._
    named("player").withCalls(
      pathCall("/api/player", createPlayer),
      pathCall("/api/player/:id", getPlayer _),
      pathCall("/api/player", getPlayers)
    )
  }
}

case class Player(id: UUID, name: String)

object Player {
  implicit val format: Format[Player] = Json.format
}

case class CreatePlayer(name: String)

object CreatePlayer {
  implicit val format: Format[CreatePlayer] = Json.format
}


