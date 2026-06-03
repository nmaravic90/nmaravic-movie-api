package com.nmaravic.movie.api.mapper;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.database.entitymodel.MovieReview;
import com.nmaravic.movie.api.model.ReviewPageResponse;
import com.nmaravic.movie.api.model.ReviewRequest;
import com.nmaravic.movie.api.model.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class MovieReviewMapper {

    public MovieReview mapReview(Movie movie, String username, ReviewRequest request) {
        MovieReview movieReview = new MovieReview();
        movieReview.setMovie(movie);
        movieReview.setUsername(username);
        movieReview.setRating(request.getRating());
        movieReview.setComment(request.getComment());
        return movieReview;
    }

    public ReviewResponse mapReviewResponse(MovieReview movieReview) {
        return new ReviewResponse()
                .id(movieReview.getReviewId())
                .movieId(movieReview.getMovie().getMovieId())
                .username(movieReview.getUsername())
                .rating(movieReview.getRating())
                .comment(movieReview.getComment())
                .createdAt(movieReview.getCreatedAt().toLocalDate());
    }

    public ReviewPageResponse mapReviewPageResponse(Page<MovieReview> result) {
        return new ReviewPageResponse()
                .content(result.getContent().stream()
                        .map(this::mapReviewResponse)
                        .toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages());
    }
}
