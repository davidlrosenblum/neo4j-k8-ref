package neo4j // 1

import io.gatling.core.Predef._ // 2
import io.gatling.http.Predef._
import scala.concurrent.duration._

class closedSimulation extends Simulation { // 3

  val NEO4J_USER = sys.env.getOrElse("NEO4J_USER","neo4j")
  val NEO4J_PASSWORD = sys.env.getOrElse("NEO4J_PASSWORD","neo4j")
  val NEO4J_URI = sys.env.getOrElse("NEO4J_URI","http://localhost:7474")
  val REQUEST_FILE = sys.env.getOrElse("REQUEST_FILE","export.csv")

  printf ("NEO4J_URI %s",NEO4J_URI)
  printf ("NEO4J_PASSWORD %s",NEO4J_PASSWORD)

  val httpProtocol = http
     // .baseUrl("https://<neo4j-hostname>:7473")
	  .baseUrl(NEO4J_URI)
      .acceptHeader("application/json")
      .basicAuth(NEO4J_USER, NEO4J_PASSWORD)
	   .shareConnections
    // Use a data file for our requests and repeat values if we get to the end.
    val feeder = csv("export.csv").circular

    // The cypher queries we will test
    val count =
      """call NBA.findTeamRecommendationsSimilarFans($email )  yield nodes unwind nodes as node return node"""


  val cypherQuery =
    """{"statements" : [{"statement" : "%s", "parameters" : { "email": "${email}" }}]}"""
      .format(count)



  val scn = scenario("closedSimulation")
    .during(30 ) {
      feed(feeder)
        .exec(
          http("Lookup")
            .post("/db/fan/tx/commit")
            .asJson
            .body(StringBody(cypherQuery))
            .check(status.is(200))
        )
    }.pause(1.seconds)




setUp(scn.inject(constantUsersPerSec(75) during (1 minutes))).throttle(
reachRps(50) in (30 seconds),
holdFor(1 minutes)//,
//reachRps(12000) in (30 seconds),
//holdFor(5 minutes)
).protocols(httpProtocol)
}
