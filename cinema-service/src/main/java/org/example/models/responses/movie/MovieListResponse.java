package org.example.models.responses.movie;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MovieListResponse {
    private final boolean isSuccess = true;
    private final List<MovieResponse> movies;
}
