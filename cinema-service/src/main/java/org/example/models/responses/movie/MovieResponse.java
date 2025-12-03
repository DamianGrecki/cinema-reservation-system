package org.example.models.responses.movie;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MovieResponse {
    private final Long id;
    private final String title;
    private final String description;
}
