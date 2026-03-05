package pl.dgrecki.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;

public class BrowsingSimulation extends BaseSimulation {

    ChainBuilder getMovies = exec(
            http("GET /api/movies")
                    .get("/api/movies")
                    .check(status().is(200))
                    .check(jsonPath("$.movies[*].id").exists()));

    ScenarioBuilder browsingScenario = scenario("Przeglądanie oferty")
            .exec(getMovies)
            .pause(1, 3)
            .exec(getScreenings)
            .exec(pickRandomScreening)
            .pause(1, 3)
            .exec(getSeatMap)
            .pause(1, 2);

    {
        setUp(
                browsingScenario.injectOpen(
                        rampUsersPerSec(1).to(20).during(30),
                        constantUsersPerSec(20).during(60),
                        rampUsersPerSec(20).to(1).during(15)))
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().percentile3().lt(500),
                        global().failedRequests().percent().lt(1.0));
    }
}
