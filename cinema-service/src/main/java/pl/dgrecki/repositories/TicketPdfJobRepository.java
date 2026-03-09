package pl.dgrecki.repositories;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.TicketPdfJob;
import pl.dgrecki.models.enums.TicketPdfStatus;

@Repository
public interface TicketPdfJobRepository extends JpaRepository<TicketPdfJob, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
        SELECT j FROM TicketPdfJob j
        WHERE j.status = :pending
           OR (j.status = :failed AND j.attempts < :maxAttempts)
        ORDER BY j.createdAt
    """)
    List<TicketPdfJob> findReadyToProcess(
            @Param("pending") TicketPdfStatus pending,
            @Param("failed") TicketPdfStatus failed,
            @Param("maxAttempts") int maxAttempts,
            Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE TicketPdfJob j
        SET j.status = :status
        WHERE j.id IN :ids
            AND j.status != :status
    """)
    int setJobsStatus(@Param("ids") List<UUID> ids, @Param("status") TicketPdfStatus status);

    @Query("""
        SELECT COUNT(j)
        FROM TicketPdfJob j
        WHERE j.ticketId IN (SELECT t.id FROM Ticket t WHERE t.order.id = :orderId)
            AND j.status != :status
    """)
    long countNotInStatusByOrderId(@Param("orderId") UUID orderId, @Param("status") TicketPdfStatus status);
}
