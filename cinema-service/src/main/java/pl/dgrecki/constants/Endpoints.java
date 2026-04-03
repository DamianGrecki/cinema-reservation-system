package pl.dgrecki.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Endpoints {

    public static final String API = "/api";

    public static final String MOVIES_ENDPOINT = API + "/movies";
    public static final String MOVIE_POSTER_UPLOAD_ENDPOINT = MOVIES_ENDPOINT + "/{movieId}/poster";
    public static final String MOVIE_POSTER_DOWNLOAD_ENDPOINT = MOVIES_ENDPOINT + "/{movieId}/poster";
    public static final String SCREENINGS_ENDPOINT = API + "/screenings";
    public static final String SCREENINGS_SEATS_ENDPOINT = SCREENINGS_ENDPOINT + "/{screeningId}/seats";
    public static final String BASKET_ENDPOINT = API + "/basket";
    public static final String BASKET_PRICING_ENDPOINT = BASKET_ENDPOINT + "/{basketId}/pricing";
    public static final String RESERVATION_ENDPOINT = API + "/reservation";
    public static final String RESERVATIONS_CANCEL_ENDPOINT = API + "/reservations/cancel";
    public static final String RESERVATION_PRICING_TYPE_ENDPOINT = RESERVATION_ENDPOINT + "/{id}/pricing-type";
    public static final String TICKET_ENDPOINT = API + "/ticket";
    public static final String ORDER_TICKETS_DOWNLOAD_ENDPOINT = API + "/orders/{orderId}/tickets/download";
    public static final String PAYMENT_ENDPOINT = API + "/payment";
}
