package pl.dgrecki.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.Refund;
import pl.dgrecki.models.enums.RefundStatus;
import pl.dgrecki.models.enums.TicketPdfStatus;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    boolean existsByPaymentAttemptIdAndStatus(UUID paymentAttemptId, RefundStatus status);

    @Query("""
        SELECT DISTINCT r.paymentAttempt.order.id FROM Refund r
        WHERE r.paymentAttempt.order.id IN :orderIds
            AND r.status = :status
    """)
    Set<UUID> findOrderIdsWithRefundStatus(
            @Param("orderIds") Collection<UUID> orderIds, @Param("status") RefundStatus status);

    @Query("""
        SELECT DISTINCT t.order.id FROM TicketPdfJob j
        JOIN Ticket t ON t.id = j.ticketId
        WHERE j.status = :jobStatus
            AND NOT EXISTS (
                SELECT 1 FROM Refund r
                WHERE r.paymentAttempt.order.id = t.order.id
                    AND r.status = :completedRefundStatus
            )
            AND (
                SELECT COUNT(r) FROM Refund r
                WHERE r.paymentAttempt.order.id = t.order.id
                    AND r.status = :failedRefundStatus
            ) < :maxAttempts
    """)
    List<UUID> findOrderIdsReadyToRefund(
            @Param("jobStatus") TicketPdfStatus jobStatus,
            @Param("completedRefundStatus") RefundStatus completedRefundStatus,
            @Param("failedRefundStatus") RefundStatus failedRefundStatus,
            @Param("maxAttempts") long maxAttempts);
}
