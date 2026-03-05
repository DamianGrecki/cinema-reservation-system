package pl.dgrecki.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public abstract class BaseSimulation extends Simulation {

    protected static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");

    protected HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    protected ChainBuilder getScreenings = exec(
            http("GET /api/screenings")
                    .get("/api/screenings")
                    .check(status().is(200))
                    .check(jsonPath("$.screenings[*].id").findAll().saveAs("screeningIds")));

    protected ChainBuilder pickRandomScreening = exec(session -> {
        java.util.List<String> ids = session.getList("screeningIds");
        int randomIdx = java.util.concurrent.ThreadLocalRandom.current().nextInt(ids.size());
        return session.set("screeningId", ids.get(randomIdx));
    });

    protected ChainBuilder getSeatMap = exec(
            http("GET /api/screenings/{id}/seats")
                    .get("/api/screenings/#{screeningId}/seats")
                    .check(status().is(200))
                    .check(jsonPath("$[0].seats[0].seatId").exists()));

    protected ChainBuilder createBasket = exec(
            http("POST /api/basket")
                    .post("/api/basket")
                    .check(status().is(201))
                    .check(jsonPath("$.basketId").saveAs("basketId")));
}
