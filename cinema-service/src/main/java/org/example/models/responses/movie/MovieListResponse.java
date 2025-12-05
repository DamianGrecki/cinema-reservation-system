package org.example.models.responses.movie;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MovieListResponse {
    private List<MovieResponse> movies;
}
