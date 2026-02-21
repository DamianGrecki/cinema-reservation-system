package pl.dgrecki.repositories;

import java.util.UUID;

public interface RowWithSeatAndStatus {
    int getRowNumber();

    int getSeatNumber();

    UUID getSeatId();

    boolean getReserved();
}
