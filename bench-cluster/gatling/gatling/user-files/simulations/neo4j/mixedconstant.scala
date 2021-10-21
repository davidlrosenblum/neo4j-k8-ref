package neo4j // 1

import io.gatling.core.Predef._ // 2
import io.gatling.http.Predef._
import scala.concurrent.duration._

class mixedConstantSimulation extends Simulation { // 3

  val httpProtocol = http
     // .baseUrl("https://<neo4j-hostname>:7473")
	  .baseUrl("http://34.136.197.149:7474")
      .acceptHeader("application/json")
      .basicAuth("<username>", "<password>")
	   .shareConnections
    // Use a data file for our requests and repeat values if we get to the end.
    val feeder = csv("export.csv").circular

    // The cypher queries we will test
    val similarTeams =
      """call NBA.findTeamRecommendationsSimilarFans($email )  yield nodes unwind nodes as node return node"""
	  
    val similarPlayers =
      """NBA.findPlayerRecommendationsSimilarFans($email )  yield nodes unwind nodes as node return node"""

  val cypherTeams =
    """{"statements" : [{"statement" : "%s", "parameters" : { "email": "${email}" }}]}"""
      .format(similarTeams)


  val cypherPlayers =
    """{"statements" : [{"statement" : "%s", "parameters" : { "email": "${email}" }}]}"""
      .format(similarPlayers)


  val scn1 = scenario("similarTeams")
    .during(300 ) {
      feed(feeder)
        .exec(
          http("similarTeams")
            .post("/db/fan/tx/commit")
            .asJson
            .body(StringBody(cypherTeams))
            .check(status.is(200))
        )
    }.pause(1.seconds)

  val scn2 = scenario("similarPlayers")
    .during(300 ) {
      feed(feeder)
        .exec(
          http("similarPlayers")
            .post("/db/fan/tx/commit")
            .asJson
            .body(StringBody(cypherPlayers))
            .check(status.is(200))
        )
    }.pause(1.seconds)



	
setUp(
	scn1.inject(constantConcurrentUsers(10000) during (5 minutes))
		.throttle(reachRps(10000) in (10 seconds),holdFor(5 minutes))
		.protocols(httpProtocol),
		
	scn2.inject(constantConcurrentUsers(10000) during (5 minutes))
		.throttle(reachRps(10000) in (10 seconds),holdFor(5 minutes))
		.protocols(httpProtocol)
	)
}