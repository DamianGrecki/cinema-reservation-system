package pl.dgrecki.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
    SELECT r.order
    FROM Reservation r
    WHERE r.basket.id = :basketId
        AND r.order IS NOT NULL
""")
    Optional<Order> findByBasketId(@Param("basketId") UUID basketId);
}
