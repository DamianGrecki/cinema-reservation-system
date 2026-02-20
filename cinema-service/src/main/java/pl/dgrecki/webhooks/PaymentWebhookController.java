package pl.dgrecki.webhooks;

import static pl.dgrecki.constants.Webhooks.PAYMENT_PROVIDER_WEBHOOK;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PAYMENT_PROVIDER_WEBHOOK)
@RequiredArgsConstructor
public class PaymentWebhookController {

    @PostMapping
    public void handlePaymentWebhook(@RequestBody String test) {
        System.out.println(test);
    }
}
