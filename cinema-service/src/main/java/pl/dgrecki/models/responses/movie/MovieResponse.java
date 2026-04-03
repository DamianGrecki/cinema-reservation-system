package pl.dgrecki.models.responses.movie;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MovieResponse {
    private UUID id;
    private String title;
    private String description;
    private String posterUrl;
}
