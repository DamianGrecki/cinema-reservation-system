package org.example.models.responses.movie;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MovieListResponse {
    private static final boolean IS_SUCCESS = true;
    private final List<MovieResponse> movies;
}
