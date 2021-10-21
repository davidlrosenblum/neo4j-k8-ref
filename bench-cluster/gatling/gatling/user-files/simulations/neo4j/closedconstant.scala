package neo4j // 1

import io.gatling.core.Predef._ // 2
import io.gatling.http.Predef._
import scala.concurrent.duration._

class closedConstantSimulation extends Simulation { // 3

  val httpProtocol = http
     // .baseUrl("https://<neo4j-hostname>:7473")
	  .baseUrl("http://34.136.197.149:7474")
      .acceptHeader("application/json")
      .basicAuth("<username>", "<password>")
	   .shareConnections
    // Use a data file for our requests and repeat values if we get to the end.
    val feeder = csv("export.csv").circular

    // The cypher queries we will test
    val count =
      """call NBA.findTeamRecommendationsSimilarFans($email )  yield nodes unwind nodes as node return node"""
      

  val cypherQuery =
    """{"statements" : [{"statement" : "%s", "parameters" : { "email": "${email}" }}]}"""
      .format(count)



  val scn = scenario("closedConstantSimulation")
    .during(300 ) {
      feed(feeder)
        .exec(
          http("Lookup")
            .post("/db/fan/tx/commit")
            .asJson
            .body(StringBody(cypherQuery))
            .check(status.is(200))
        )
    }.pause(1.seconds)



	
setUp(scn.inject(constantConcurrentUsers(20000) during (5 minutes)))
	.throttle(reachRps(19000) in (30 seconds),holdFor(5 minutes))
	.protocols(httpProtocol)
}