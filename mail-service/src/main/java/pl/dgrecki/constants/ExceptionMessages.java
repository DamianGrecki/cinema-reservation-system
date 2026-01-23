package pl.dgrecki.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionMessages {
    public static final String INVALID_INCOMING_EVENT_MSG = "Invalid incoming event";
    public static final String MISSING_REQUIRED_FIELD_EVENT_ID_MSG = "Missing required field: eventId";
    public static final String MISSING_REQUIRED_FIELD_EVENT_TYPE_MSG = "Missing required field: eventType";
}
