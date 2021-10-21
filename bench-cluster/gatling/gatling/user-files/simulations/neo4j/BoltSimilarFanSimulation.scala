package org.neo4j.gatling.bolt.simulation

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import org.neo4j.driver.{AccessMode, AuthTokens, Config}
import org.neo4j.driver.GraphDatabase.driver
import org.neo4j.gatling.bolt.Predef._
import org.neo4j.gatling.bolt.builder.SessionHelper._

import scala.concurrent.duration._
import scala.util.Random

class BoltSimilarFanSimulation extends Simulation {

  val feeder = csv("export.csv").circular
  
  val count =
           """MATCH (f:Fan {email: $email})-[:SIMILAR]->()-[:LIKES|FOLLOWS|FAVORITE]->(team:Team) OPTIONAL MATCH (team)-[:IN_CITY]->()<-[:IN_CITY]-()<-[:HAS_ADDRESS]-(f) WHERE NOT ((f)-[:FAVORITE|FOLLOWS|LIKES]->(team)) RETURN team AS item LIMIT 25"""
  val sproc =
           """call NBA.findTeamRecommendationsSimilarFans($email) yield nodes return size(nodes);"""
  
  
  val boltProtocol = bolt(driver("neo4j+s://<neo4j-hostname>:7687", AuthTokens.basic("<username>", "<password>"), Config.builder().withMaxConnectionPoolSize(2500).build()))
  
  //val boltProtocol = bolt(driver("neo4j+s://<neo4j-hostname>:7687", AuthTokens.basic("<username>", "<password>")))
	val testScenario = scenario("simpleCreate")
        .feed(feeder)
    .exec(
        cypher(sproc,Map("email" -> "${email}") , "fan")
    ).pause(1.milliseconds)

  setUp(testScenario.inject(
    nothingFor(1 seconds), // 1
    atOnceUsers(100), // 2
    rampUsersPerSec(500) to (4000) during (60 seconds), //, 
	constantUsersPerSec(1000) during (30 seconds)
	)).protocols(boltProtocol)

}
