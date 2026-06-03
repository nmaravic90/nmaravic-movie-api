package com.nmaravic.movie.api.exception;

public class MovieAlreadyExistsException extends RuntimeException {

    public MovieAlreadyExistsException(String title, Integer year) {
        super(String.format("Movie '%s' from %d already exists!", title, year));
    }
}
