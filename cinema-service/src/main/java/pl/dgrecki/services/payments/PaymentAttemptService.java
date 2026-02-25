package pl.dgrecki.services.payments;

import static pl.dgrecki.constants.ExceptionMessages.PAYMENT_ATTEMPT_NOT_FOUND_MSG;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.exceptions.ResourceNotFoundException;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.PaymentAttempt;
import pl.dgrecki.models.enums.PaymentStatus;
import pl.dgrecki.repositories.PaymentAttemptRepository;

@Service
public class PaymentAttemptService {

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final Clock clock;
    private final int attemptsLimit;

    public PaymentAttemptService(
            PaymentAttemptRepository paymentAttemptRepository,
            Clock clock,
            @Value("${payment-provider.attempts-limit}") int attemptsLimit) {
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.clock = clock;
        this.attemptsLimit = attemptsLimit;
    }

    @Transactional
    public void createAttempt(Order order, String transactionId, PaymentStatus status, String providerError) {
        PaymentAttempt attempt = PaymentAttempt.builder()
                .order(order)
                .transactionId(transactionId)
                .status(status)
                .provider(order.getProvider())
                .providerError(providerError)
                .createdAt(clock.instant())
                .build();
        paymentAttemptRepository.save(attempt);
    }

    public boolean isAttemptsLimitReached(Order order) {
        return getAttemptCountByOrder(order) >= attemptsLimit;
    }

    public boolean hasPendingPaymentAttempt(Order order) {
        return paymentAttemptRepository.existsByOrderAndStatus(order, PaymentStatus.PENDING);
    }

    @Transactional
    public PaymentAttempt updateAttemptStatus(String transactionId, PaymentStatus status) {
        PaymentAttempt attempt = paymentAttemptRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(String.format(PAYMENT_ATTEMPT_NOT_FOUND_MSG, transactionId)));
        attempt.setStatus(status);
        attempt.setUpdatedAt(clock.instant());
        return attempt;
    }

    private int getAttemptCountByOrder(Order order) {
        return paymentAttemptRepository.countPaymentAttemptByOrder(order);
    }
}
