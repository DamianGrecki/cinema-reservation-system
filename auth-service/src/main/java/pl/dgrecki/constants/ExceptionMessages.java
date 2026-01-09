package pl.dgrecki.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionMessages {

    public static final String INCORRECT_CREDENTIALS_MSG = "Incorrect credentials";
    public static final String ROLE_NOT_FOUND_MSG = "Customer role not found";
    public static final String USER_NOT_FOUND_MSG = "User not found";
    public static final String TOKEN_NOT_FOUND_MSG = "Token not found";
    public static final String TOKEN_EXPIRED_MSG = "Token has expired";
    public static final String TOKEN_IS_USED_MSG = "Token has already been used";
    public static final String EVENT_DATA_SERIALIZE_FAILED_MSG = "Failed to serialize Event Data";
}
