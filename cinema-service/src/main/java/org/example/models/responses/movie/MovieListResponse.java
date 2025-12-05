package org.example.models.responses.movie;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MovieListResponse {
    private final boolean isSuccess = true;
    private final List<MovieResponse> movies;
}
