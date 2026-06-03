package com.nmaravic.movie.api.exception;

public class MovieImageLimitException extends RuntimeException {
    public MovieImageLimitException(String message) {
        super(message);
    }
}
