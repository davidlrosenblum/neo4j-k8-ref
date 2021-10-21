package org.neo4j.gatling.bolt.simulation

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import org.neo4j.driver.{AccessMode, AuthTokens,Config}
import org.neo4j.driver.GraphDatabase.driver
import org.neo4j.gatling.bolt.Predef._
import org.neo4j.gatling.bolt.builder.SessionHelper._

import scala.concurrent.duration._
import scala.util.Random

class BoltSimilarFanSimulation2 extends Simulation {

  val feeder = csv("export.csv").circular
  
  val sproc =
           """call NBA.findTeamRecommendationsSimilarFans($email) yield nodes return size(nodes);"""
  
  val boltProtocol = bolt(driver("neo4j+s://<neo4j-hostname>:7687", AuthTokens.basic("<username>", "<password>"), Config.builder().withMaxConnectionPoolSize(35000).build()))

val testScenario2 = scenario("LongerSimulation")
    .during(300 ) {
      feed(feeder)
        .exec(
          cypher(sproc,Map("email" -> "${email}") , "fan")
        )
    }
	
setUp(testScenario2.inject(
    nothingFor(1 seconds), // 1
    atOnceUsers(100), // 2
    rampUsers(34000) during (60 seconds))).protocols(boltProtocol)

}
