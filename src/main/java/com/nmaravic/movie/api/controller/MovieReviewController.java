package com.nmaravic.movie.api.controller;

import com.nmaravic.movie.api.MovieReviewApi;
import com.nmaravic.movie.api.model.ReviewPageResponse;
import com.nmaravic.movie.api.model.ReviewRequest;
import com.nmaravic.movie.api.model.ReviewResponse;
import com.nmaravic.movie.api.service.MovieReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class MovieReviewController implements MovieReviewApi {

    private final MovieReviewService movieReviewService;

    public MovieReviewController(MovieReviewService movieReviewService) {
        this.movieReviewService = movieReviewService;
    }

    @Override
    public ResponseEntity<ReviewResponse> createReview(Long movieId, ReviewRequest reviewRequest) {
        String username = getCurrentUsername();
        ReviewResponse reviewResponse = movieReviewService.createReview(movieId, username, reviewRequest);
        return new ResponseEntity<>(reviewResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ReviewPageResponse> getMovieReviews(Long movieId, Integer page, Integer size) {
        ReviewPageResponse reviewPageResponse = movieReviewService.getMovieReviews(movieId, page, size);
        return new ResponseEntity<>(reviewPageResponse, HttpStatus.OK);
    }


    @Override
    public ResponseEntity<Void> deleteReview(Long movieId, Long reviewId) {
        movieReviewService.deleteReview(movieId, reviewId);
        return ResponseEntity.noContent().build();
    }

    private String getCurrentUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(auth -> ((JwtAuthenticationToken) auth).getToken().getClaimAsString("preferred_username"))
                .orElse("anonymous");
    }
}
