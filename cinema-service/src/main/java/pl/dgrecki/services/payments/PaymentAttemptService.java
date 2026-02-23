package pl.dgrecki.services.payments;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.PaymentAttempt;
import pl.dgrecki.models.enums.PaymentStatus;
import pl.dgrecki.repositories.PaymentAttemptRepository;

@Service
@RequiredArgsConstructor
public class PaymentAttemptService {

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final Clock clock;

    @Transactional
    public void createAttempt(Order order, String transactionId, PaymentStatus status, String providerError) {
        PaymentAttempt attempt = PaymentAttempt.builder()
                .order(order)
                .transactionId(transactionId)
                .status(status)
                .providerError(providerError)
                .createdAt(clock.instant())
                .build();
        paymentAttemptRepository.save(attempt);
    }
}
