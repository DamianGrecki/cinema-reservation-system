package pl.dgrecki.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionMessages {
    public static final String INVALID_OR_EXPIRED_TOKEN_MSG = "Invalid or expired token.";
    public static final String ACCESS_DENIED_MSG = "You do not have permission to access this resource.";
    public static final String RESERVATION_ALREADY_EXISTS_MSG = "Reservation already exists.";
    public static final String SEAT_NOT_FOUND_MSG = "Seat not found.";
    public static final String SCREENING_NOT_FOUND_MSG = "Screening not found.";
    public static final String SEAT_DOES_NOT_BELONG_TO_HALL_MSG = "Seat does not belong to the screening's cinema hall";
    public static final String SHOW_HAS_ALREADY_STARTED_MSG = "Reservation failed the show has already started";
}
