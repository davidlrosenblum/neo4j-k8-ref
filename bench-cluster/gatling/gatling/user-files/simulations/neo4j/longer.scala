package neo4j // 1

import io.gatling.core.Predef._ // 2
import io.gatling.http.Predef._
import scala.concurrent.duration._

class LongerSimulation extends Simulation { // 3

  val httpProtocol = http
     // .baseUrl("https://<neo4j-hostname>:7473")
	  .baseUrl("http://34.136.197.149:7474")
      .acceptHeader("application/json")
      .basicAuth("<username>", "<password>")
    // Use a data file for our requests and repeat values if we get to the end.
    val feeder = csv("export.csv").circular

    // The cypher queries we will test
    val count =
      """call NBA.findTeamRecommendationsSimilarFans($email )  yield nodes unwind nodes as node return node"""
      

  val cypherQuery =
    """{"statements" : [{"statement" : "%s", "parameters" : { "email": "${email}" }}]}"""
      .format(count)



  val scn = scenario("LongerSimulation")
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
	
  setUp(scn.inject(
    nothingFor(1 seconds), // 1
    atOnceUsers(1000), // 2
    rampUsers(11000) during (60 seconds))).protocols(httpProtocol)
}