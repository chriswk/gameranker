package com.chriswk.gamerankerstream.api

import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

/**
  * The Gameranker stream interface.
  *
  * This describes everything that Lagom needs to know about how to serve and
  * consume the GamerankerStream service.
  */
trait GamerankerStreamService extends Service {

  def stream: ServiceCall[Source[String, _], Source[String, _]]

  override final def descriptor = {
    import Service._

    named("gameranker-stream")
      .withCalls(
        namedCall("stream", stream)
      ).withAutoAcl(true)
  }
}

