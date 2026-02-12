package pl.dgrecki.repositories;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.Basket;

@Repository
public interface BasketRepository extends JpaRepository<Basket, UUID> {
    @Query(value = """
    SELECT b.id
    FROM Basket b
    WHERE b.expiresAt < :instant
    ORDER BY b.expiresAt ASC
""")
    List<UUID> findIdsByExpiresAtBefore(@Param("instant") Instant instant, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query(value = """
    DELETE Basket b
    WHERE b.id IN :ids
        AND b.expiresAt < :instant
""")
    int deleteExpiredBasketsWithReservations(@Param("ids") List<UUID> ids, @Param("instant") Instant instant);
}
