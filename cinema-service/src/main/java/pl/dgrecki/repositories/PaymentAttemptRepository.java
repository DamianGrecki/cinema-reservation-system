package pl.dgrecki.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.Order;
import pl.dgrecki.models.entities.PaymentAttempt;
import pl.dgrecki.models.enums.PaymentStatus;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {

    int countPaymentAttemptByOrder(Order order);

    boolean existsByOrderAndStatus(Order order, PaymentStatus paymentStatus);

    Optional<PaymentAttempt> findByTransactionId(String transactionId);
}
