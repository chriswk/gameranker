package com.chriswk.gameranker.player.impl

import java.util.UUID

import akka.actor.ActorSystem
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.chriswk.gameranker.player.api
import com.chriswk.gameranker.player.api.PlayerService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.ExecutionContext

class PlayerServiceImpl(registry: PersistentEntityRegistry, system: ActorSystem)(implicit ec: ExecutionContext, mat: Materializer) extends PlayerService {
  private val currentIdsQuery = PersistenceQuery(system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  override def createPlayer = ServiceCall { createPlayer =>
    val playerId = UUID.randomUUID()
    refFor(playerId).ask(CreatePlayer(createPlayer.name)).map { _ =>
      api.Player(playerId, createPlayer.name)
    }
  }

  override def getPlayer(playerId: UUID) = ServiceCall { _ =>
    refFor(playerId).ask(GetPlayer).map {
      case Some(player) => api.Player(playerId, player.name)
      case None => throw NotFound(s"Player with id $playerId")
    }

  }

  private def refFor(playerId: UUID) = registry.refFor[PlayerEntity](playerId.toString)

  override def getPlayers = ServiceCall { _ =>
    currentIdsQuery.currentPersistenceIds()
      .filter(_.startsWith("PlayerEntity|"))
      .mapAsync(4) { id =>
        val entityId = id.split("\\|", 2).last
        registry.refFor[PlayerEntity](entityId)
          .ask(GetPlayer)
          .map(_.map(player => api.Player(UUID.fromString(entityId), player.name)))
      }
      .collect {
        case Some(p) => p
      }
      .runWith(Sink.seq)
  }
}
