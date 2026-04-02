package pl.dgrecki.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Endpoints {
    public static final String LOGIN_ENDPOINT = "/api/user/login";
    public static final String REGISTER_CUSTOMER_ENDPOINT = "/api/user/register/customer";
    public static final String USER_ACTIVATE_ENDPOINT = "/api/user/activate";
    public static final String INTERNAL_TOKEN_ENDPOINT = "/api/internal/token";
    public static final String INTERNAL_USER_ENDPOINT = "/api/internal/users/{id}";
    public static final String REFRESH_TOKEN_ENDPOINT = "/api/token/refresh";
    public static final String LOGOUT_ENDPOINT = "/api/token/logout";
}
