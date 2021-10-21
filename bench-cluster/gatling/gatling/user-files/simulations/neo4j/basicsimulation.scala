package neo4j // 1

import io.gatling.core.Predef._ // 2
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicSimulation extends Simulation { // 3

  val httpProtocol = http
      .baseUrl("https://<neo4j-hostname>:7473")
      .acceptHeader("application/json")
      .basicAuth("<username>", "<password>")
    // Use a data file for our requests and repeat values if we get to the end.
    val feeder = csv("export.csv").circular

    // The cypher queries we will test
    val count =
      """MATCH (f:Fan {email: $email})-[:SIMILAR]->()-[:LIKES|FOLLOWS|FAVORITE]->(team:Team) OPTIONAL MATCH (team)-[:IN_CITY]->()<-[:IN_CITY]-()<-[:HAS_ADDRESS]-(f) WHERE NOT ((f)-[:FAVORITE|FOLLOWS|LIKES]->(team)) RETURN team AS item LIMIT 25"""
      

  val cypherQuery =
    """{"statements" : [{"statement" : "%s", "parameters" : { "email": "${email}" }}]}"""
      .format(count)



  val scn = scenario("BasicSimulation")
    .during(600 ) {
      feed(feeder)
        .exec(
          http("Lookup")
            .post("/db/fan/tx/commit")
            .asJson
            .body(StringBody(cypherQuery))
            .check(status.is(200))
        )
    }
  setUp(
    scn.inject(atOnceUsers(1500)), //users.inject(rampUsers(1500) over (3000 seconds)),
  ).protocols(httpProtocol)
}