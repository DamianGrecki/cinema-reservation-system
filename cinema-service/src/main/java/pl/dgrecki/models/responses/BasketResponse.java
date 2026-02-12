package pl.dgrecki.models.responses;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BasketResponse {
    private final UUID basketId;
}
