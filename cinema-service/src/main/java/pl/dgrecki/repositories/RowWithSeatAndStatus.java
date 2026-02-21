package pl.dgrecki.repositories;

import java.util.UUID;

public interface RowWithSeatAndStatus {
    Integer getRowNumber();

    Integer getSeatNumber();

    UUID getSeatId();

    Boolean getReserved();
}
