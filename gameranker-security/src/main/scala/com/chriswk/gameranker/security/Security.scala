package com.chriswk.gameranker.security

import java.security.Principal
import java.util.UUID
import javax.security.auth.Subject

import com.lightbend.lagom.scaladsl.api.security.ServicePrincipal
import com.lightbend.lagom.scaladsl.api.transport._
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

sealed trait PlayerPrincipal extends Principal {
  val playerId: UUID
  override def getName: String = playerId.toString
  override def implies(subject: Subject): Boolean = false
}

object PlayerPrincipal {
  case class ServicelessPlayerPrincipal(playerId: UUID) extends PlayerPrincipal
  case class PlayerServicePrincipal(playerId: UUID, servicePrincipal: ServicePrincipal) extends PlayerPrincipal with ServicePrincipal {
    override def serviceName: String = servicePrincipal.serviceName
  }

  def of(playerId: UUID, principal: Option[Principal]) = {
    principal match {
      case Some(servicePrincipal: ServicePrincipal) =>
        PlayerPrincipal.PlayerServicePrincipal(playerId, servicePrincipal)
      case _ => PlayerPrincipal.ServicelessPlayerPrincipal(playerId)
    }
  }
}

object SecurityHeaderFilter extends HeaderFilter {
  override def transformClientRequest(request: RequestHeader) = {
    request.principal match {
      case Some(playerPrincipal: PlayerPrincipal) => request.withHeader("User-Id", playerPrincipal.playerId.toString)
      case _ => request
    }
  }

  override def transformServerRequest(request: RequestHeader) = {
    request.getHeader("User-Id") match {
      case Some(playerId) => request.withPrincipal(PlayerPrincipal.of(UUID.fromString(playerId), request.principal))
      case None => request
    }
  }

  override def transformServerResponse(response: ResponseHeader, request: RequestHeader) = response

  override def transformClientResponse(response: ResponseHeader, request: RequestHeader) = response

  lazy val Composed = HeaderFilter.composite(SecurityHeaderFilter, UserAgentHeaderFilter)
}

object ServerSecurity {

  def authenticated[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]) =
    ServerServiceCall.compose { requestHeader =>
      requestHeader.principal match {
        case Some(playerPrincipal: PlayerPrincipal) =>
          serviceCall(playerPrincipal.playerId)
        case other =>
          throw Forbidden("User not authenticated")
      }
    }

}

object ClientSecurity {

  /**
    * Authenticate a client request.
    */
  def authenticate(userId: UUID): RequestHeader => RequestHeader = { request =>
    request.withPrincipal(PlayerPrincipal.of(userId, request.principal))
  }
}