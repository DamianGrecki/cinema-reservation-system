package pl.dgrecki.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Webhooks {

    public static final String WEBHOOK_BASE = "/api/webhook";
    public static final String PAYMENT_PROVIDER_WEBHOOK = WEBHOOK_BASE + "/payment";
}
