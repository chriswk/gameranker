package com.chriswk.gameranker.player.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{Jsonable, SerializerRegistry, Serializers}
import play.api.libs.json.{Format, Json}
import com.chriswk.gameranker.utils.JsonFormats._

class PlayerEntity extends PersistentEntity {
  override type Command = PlayerCommand
  override type Event = PlayerEvent
  override type State = Option[Player]
  override def initialState = None
  override def behavior: Behavior = {
    case Some(player) =>
      Actions().onReadOnlyCommand[GetPlayer.type, Option[Player]]{
        case (GetPlayer, ctx, state) => ctx.reply(state)
      }.onReadOnlyCommand[CreatePlayer, Done] {
        case (CreatePlayer(name), ctx, state) => ctx.invalidCommand("Player already exists")
      }
    case None =>
      Actions().onReadOnlyCommand[GetPlayer.type, Option[Player]] {
        case (GetPlayer, ctx, state) => ctx.reply(state)
      }.onCommand[CreatePlayer, Done] {
        case (CreatePlayer(name), ctx, state) =>
          ctx.thenPersist(PlayerCreated(name), _ => ctx.reply(Done))
      }.onEvent {
        case (PlayerCreated(name), state) => Some(Player(name))
      }
  }
}

case class Player(name: String) extends Jsonable

object Player {
  implicit val format: Format[Player] = Json.format
}

sealed trait PlayerEvent extends Jsonable

case class PlayerCreated(name: String) extends PlayerEvent

object PlayerCreated {
  implicit val format: Format[PlayerCreated] = Json.format
}

sealed trait PlayerCommand extends Jsonable

case class CreatePlayer(name: String) extends PlayerCommand with ReplyType[Done]

object CreatePlayer {
  implicit val format: Format[CreatePlayer] = Json.format
}

case object GetPlayer extends PlayerCommand with ReplyType[Option[Player]] {
  implicit val format: Format[GetPlayer.type] = singletonFormat(GetPlayer)
}

class PlayerSerializerRegistry extends SerializerRegistry {
  override def serializers = List(
    Serializers[Player],
    Serializers[PlayerCreated],
    Serializers[CreatePlayer],
    Serializers[GetPlayer.type]
  )
}