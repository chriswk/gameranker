package com.chriswk.gameranker.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.chriswk.gameranker.api.GamerankerService

/**
  * Implementation of the GamerankerService.
  */
class GamerankerServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends GamerankerService {

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the Gameranker entity for the given ID.
    val ref = persistentEntityRegistry.refFor[GamerankerEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id, None))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the Gameranker entity for the given ID.
    val ref = persistentEntityRegistry.refFor[GamerankerEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message))
  }
}
