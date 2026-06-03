package com.nmaravic.movie.api.exception;

public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(Long id) {
        super(String.format("Movie with id %d not found!", id));
    }
}
