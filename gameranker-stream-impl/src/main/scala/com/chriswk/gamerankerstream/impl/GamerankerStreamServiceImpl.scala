package com.chriswk.gamerankerstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.chriswk.gamerankerstream.api.GamerankerStreamService
import com.chriswk.gameranker.api.GamerankerService

import scala.concurrent.Future

/**
  * Implementation of the GamerankerStreamService.
  */
class GamerankerStreamServiceImpl(gamerankerService: GamerankerService) extends GamerankerStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(gamerankerService.hello(_).invoke()))
  }
}
