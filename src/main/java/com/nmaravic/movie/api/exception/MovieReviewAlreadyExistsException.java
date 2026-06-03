package com.nmaravic.movie.api.exception;

public class MovieReviewAlreadyExistsException extends RuntimeException {
    public MovieReviewAlreadyExistsException(String message) {
        super(message);
    }

    public MovieReviewAlreadyExistsException(Long movieId, String username) {
        super(String.format("User '%s' has already reviewed movie with id %d", username, movieId));
    }
}
