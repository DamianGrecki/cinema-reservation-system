package pl.dgrecki.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dgrecki.models.entities.PriceModifier;

@Repository
public interface PriceModifierRepository extends JpaRepository<PriceModifier, Long> {}
