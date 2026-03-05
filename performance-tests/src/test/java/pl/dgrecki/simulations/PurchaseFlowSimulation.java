package pl.dgrecki.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;

public class PurchaseFlowSimulation extends BaseSimulation {

    ChainBuilder findAvailableSeat = exec(session -> {
                java.util.List<String> ids = new java.util.ArrayList<>(session.getList("screeningIds"));
                java.util.Collections.shuffle(ids);
                return session.set("shuffledScreeningIds", ids).set("seatFound", false).set("retryCount", 0);
            })
            .asLongAs(session -> !session.getBoolean("seatFound") && session.getInt("retryCount") < 3)
            .on(
                    exec(session -> {
                        java.util.List<String> ids = session.getList("shuffledScreeningIds");
                        int attempt = session.getInt("retryCount");
                        if (attempt >= ids.size()) {
                            return session.markAsFailed();
                        }
                        return session.set("screeningId", ids.get(attempt));
                    })
                    .exec(http("GET /api/screenings/{id}/seats")
                            .get("/api/screenings/#{screeningId}/seats")
                            .check(status().is(200))
                            .check(jsonPath("$[*].seats[?(@.reserved==false)].seatId")
                                    .findAll()
                                    .optional()
                                    .saveAs("availableSeats")))
                    .exec(session -> {
                        java.util.List<String> seats = session.getList("availableSeats");
                        if (seats != null && !seats.isEmpty()) {
                            int randomIdx = java.util.concurrent.ThreadLocalRandom.current().nextInt(seats.size());
                            return session.set("chosenSeatId", seats.get(randomIdx)).set("seatFound", true);
                        }
                        return session.set("retryCount", session.getInt("retryCount") + 1);
                    })
            );

    ChainBuilder createReservation = exec(
            http("POST /api/reservation")
                    .post("/api/reservation")
                    .body(StringBody(
                            """
                    {
                        "basketId": "#{basketId}",
                        "screeningId": "#{screeningId}",
                        "seatId": "#{chosenSeatId}"
                    }
                    """))
                    .check(status().is(201))
                    .check(jsonPath("$.reservationId").saveAs("reservationId")));

    ChainBuilder changePricingType = exec(
            http("PATCH /api/reservation/{id}/pricing-type")
                    .patch("/api/reservation/#{reservationId}/pricing-type")
                    .body(StringBody("""
                    {
                        "pricingType": "REDUCED"
                    }
                    """))
                    .check(status().is(200)));

    ChainBuilder createPayment = exec(session -> {
                String guestEmail = "guest_" + session.userId() + "_" + System.currentTimeMillis() + "@example.com";
                return session.set("guestEmail", guestEmail);
            })
            .exec(http("POST /api/payment")
                    .post("/api/payment")
                    .body(StringBody(
                            """
                    {
                        "basketId": "#{basketId}",
                        "provider": "SANDBOX",
                        "guestEmail": "#{guestEmail}"
                    }
                    """))
                    .check(status().is(201)));

    ScenarioBuilder purchaseScenario = scenario("Flow zakupowy (gość)")
            .exec(getScreenings)
            .pause(2, 5)
            .exec(findAvailableSeat)
            .doIf(session -> session.getBoolean("seatFound")).then(
                    exec(createBasket)
                    .pause(1, 2)
                    .exec(createReservation)
                    .exitHereIfFailed()
                    .pause(1, 3)
                    .randomSwitch().on(
                            new Choice.WithWeight(30, exec(changePricingType)),
                            new Choice.WithWeight(70, exec(session -> session)))
                    .pause(2, 5)
                    .exec(createPayment)
            );

    {
        setUp(
                purchaseScenario.injectOpen(
                        rampUsersPerSec(1).to(10).during(15),
                        constantUsersPerSec(10).during(60),
                        rampUsersPerSec(10).to(1).during(15)))
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().percentile3().lt(1000),
                        global().failedRequests().percent().lt(5.0));
    }
}
