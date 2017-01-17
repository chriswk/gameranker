package com.chriswk.gameranker.player

import akka.actor.ActorSystem
import com.chriswk.gameranker.player.impl.{CreatePlayer, Player, PlayerCreated, PlayerEntity}
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.typesafe.config.ConfigFactory
import org.scalactic.ConversionCheckedTripleEquals
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

class PlayerSpec extends WordSpecLike with Matchers with BeforeAndAfterAll with ConversionCheckedTripleEquals {
  val config = ConfigFactory.load()
  val system = ActorSystem("PlayerSpec", config)

  override def afterAll(): Unit = {
    Await.ready(system.terminate, 10.seconds)
  }

  "Player entity" must {
    "handle CreatePlayer" in {
      val driver = new PersistentEntityTestDriver(system, new PlayerEntity, "player-1")
      val name = "Testing hero"
      val outcome = driver.run(CreatePlayer(name))
      outcome.events should ===(List(PlayerCreated(name)))
      outcome.state should ===(Some(Player("Testing hero")))
    }

    "be idempotent" in {
      val driver = new PersistentEntityTestDriver(system, new PlayerEntity, "player-1")
      val name = "Testing hero"
      val outcome = driver.run(CreatePlayer(name), CreatePlayer(name), CreatePlayer(name))
      outcome.events should ===(List(PlayerCreated(name)))
      outcome.state should ===(Some(Player("Testing hero")))
      val secondRun = driver.run(CreatePlayer("Frank"))
      secondRun.events should ===(List())
      secondRun.state should ===(Some(Player("Testing hero")))

    }
  }
}
