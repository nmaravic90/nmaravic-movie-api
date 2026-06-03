package com.nmaravic.movie.api.exception;

public class MovieReviewNotFoundException extends RuntimeException {
    public MovieReviewNotFoundException(String message) {
        super(message);
    }

    public MovieReviewNotFoundException(Long reviewId) {
        super(String.format("Review with id %d not found", reviewId));
    }
}
