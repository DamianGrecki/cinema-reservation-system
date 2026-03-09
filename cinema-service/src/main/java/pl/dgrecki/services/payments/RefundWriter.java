package pl.dgrecki.services.payments;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.dgrecki.models.entities.PaymentAttempt;
import pl.dgrecki.models.entities.Refund;
import pl.dgrecki.models.enums.RefundReason;
import pl.dgrecki.models.enums.RefundStatus;
import pl.dgrecki.repositories.RefundRepository;

@Component
@RequiredArgsConstructor
public class RefundWriter {

    private final RefundRepository refundRepository;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedRefund(PaymentAttempt paymentAttempt, String transactionId, RefundReason reason) {
        Refund refund = Refund.builder()
                .paymentAttempt(paymentAttempt)
                .transactionId(transactionId)
                .status(RefundStatus.FAILED)
                .reason(reason)
                .createdAt(clock.instant())
                .build();
        refundRepository.save(refund);
    }
}
