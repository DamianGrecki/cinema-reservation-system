package pl.dgrecki.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;

public class ConcurrentReservationSimulation extends BaseSimulation {

    ChainBuilder getScreeningAndSeat = exec(
                    http("GET /api/screenings")
                            .get("/api/screenings")
                            .check(status().is(200))
                            .check(jsonPath("$.screenings[0].id").saveAs("screeningId")))
            .exec(http("GET /api/screenings/{id}/seats")
                    .get("/api/screenings/#{screeningId}/seats")
                    .check(status().is(200))
                    .check(jsonPath("$[*].seats[?(@.reserved==false)].seatId")
                            .find(0)
                            .saveAs("targetSeatId")));

    ChainBuilder tryReserveSameSeat = exec(createBasket)
            .exec(http("POST /api/reservation (concurrent)")
                    .post("/api/reservation")
                    .body(StringBody(
                            """
                    {
                        "basketId": "#{basketId}",
                        "screeningId": "#{screeningId}",
                        "seatId": "#{targetSeatId}"
                    }
                    """))
                    .check(status().in(201, 400, 404, 409, 422)));

    ScenarioBuilder concurrentReservation = scenario("Współbieżna rezerwacja tego samego miejsca")
            .exec(getScreeningAndSeat)
            .pause(1)
            .exec(tryReserveSameSeat);

    {
        setUp(
                concurrentReservation.injectOpen(
                        atOnceUsers(50)))
                .protocols(httpProtocol)
                .assertions(
                        global().failedRequests().percent().lt(100.0));
    }
}
