package pl.dgrecki.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionMessages {
    public static final String INVALID_OR_EXPIRED_TOKEN_MSG = "Invalid or expired token.";
    public static final String ACCESS_DENIED_MSG = "You do not have permission to access this resource.";
    public static final String RESERVATION_ALREADY_EXISTS_MSG = "Reservation already exists.";
    public static final String TICKET_ALREADY_EXISTS_MSG = "Ticket already exists.";
    public static final String SEAT_NOT_FOUND_MSG = "Seat %s not found.";
    public static final String SEAT_ID_IS_REQUIRED_MSG = "seatId is required";
    public static final String SCREENING_NOT_FOUND_MSG = "Screening %s not found.";
    public static final String SCREENING_ID_IS_REQUIRED_MSG = "ScreeningId is required";
    public static final String BASKET_NOT_FOUND_MSG = "Basket %s not found.";
    public static final String RESERVATION_NOT_FOUND_MSG = "Reservation %s not found.";
    public static final String RESERVATIONS_NOT_FOUND_MSG = "Reservations not found.";
    public static final String TICKET_NOT_FOUND_MSG = "Ticket %s not found.";
    public static final String TICKETS_BY_ORDER_NOT_FOUND_MSG = "Not found tickets with orderId: %s";
    public static final String TICKET_PDF_NOT_GENERATED_MSG = "Ticket PDF is not yet generated.";
    public static final String TICKET_TEMPLATE_NOT_FOUND_MSG = "Cannot find active ticket template";
    public static final String INVALID_TICKET_DOWNLOAD_LINK_MSG = "Invalid ticket download link";
    public static final String SEAT_DOES_NOT_BELONG_TO_HALL_MSG = "Seat does not belong to the screening's cinema hall";
    public static final String SHOW_HAS_ALREADY_STARTED_MSG = "Reservation failed the show has already started";
    public static final String BASKET_EXPIRED_MSG = "Basket has expired";
    public static final String BASKET_ID_IS_REQUIRED_MSG = "basketId is required";
    public static final String RESERVATIONS_IDS_IS_REQUIRED_MSG = "reservationsIds is required";
    public static final String RESERVATIONS_IDS_CANNOT_BE_EMPTY_MSG = "reservationsIds cannot be empty";
    public static final String RESERVATIONS_STATUS_TRANSITION_FAILED_MSG =
            "Reservations %s cannot transition to status %s";
    public static final String PRICING_TYPE_IS_REQUIRED_MSG = "pricingType is required";
    public static final String CUSTOMER_DATA_REQUIRED_MSG = "customerId or guestEmail required";
    public static final String INVALID_EMAIL_MSG = "Invalid email address";
    public static final String PROVIDER_IS_REQUIRED_MSG = "Provider required";
    public static final String PROVIDER_IS_UNKNOWN_MSG = "Unknown provider";
    public static final String PROVIDER_CANNOT_CREATE_PAYMENT_MSG = "Provider cannot create payment";
    public static final String PROVIDER_NO_RESPONSE_MSG = "No response from the provider";
    public static final String PROVIDER_BAD_PAYMENT_STATUS_MSG = "Bad payment status returned from provider";
    public static final String PAYMENT_ATTEMPTS_LIMIT_REACHED_MSG = "Payment failed, reached payment attempts limit";
    public static final String PAYMENT_ATTEMPT_ALREADY_EXISTS_MSG =
            "Payment failed, pending payment attempt already exists";
    public static final String PAYMENT_ATTEMPT_NOT_FOUND_MSG = "Payment attempt with transaction ID %s not found";
    public static final String DATA_INTEGRITY_CONFLICT_MSG = "Data integrity conflict";
    public static final String REFUND_FAILED_MSG = "Refund failed for order %s";
    public static final String REFUND_ALREADY_EXISTS_MSG = "Refund failed for order %s";
    public static final String REFUND_NO_COMPLETED_PAYMENT_MSG = "No completed payment found for order %s";
    public static final String ORDER_NOT_FOUND_MSG = "Order %s not found.";
    public static final String MOVIE_NOT_FOUND_MSG = "Movie %s not found.";
    public static final String POSTER_NOT_FOUND_MSG = "Poster not found for movie: %s";
}
