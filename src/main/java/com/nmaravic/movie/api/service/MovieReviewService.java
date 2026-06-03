package com.nmaravic.movie.api.service;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.database.entitymodel.MovieReview;
import com.nmaravic.movie.api.database.repository.MovieRepository;
import com.nmaravic.movie.api.database.repository.MovieReviewRepository;
import com.nmaravic.movie.api.exception.MovieNotFoundException;
import com.nmaravic.movie.api.exception.MovieReviewAlreadyExistsException;
import com.nmaravic.movie.api.exception.MovieReviewNotFoundException;
import com.nmaravic.movie.api.mapper.MovieReviewMapper;
import com.nmaravic.movie.api.model.ReviewPageResponse;
import com.nmaravic.movie.api.model.ReviewRequest;
import com.nmaravic.movie.api.model.ReviewResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovieReviewService {

    @Value("${movie.page.size}")
    private int pageSize;

    private final MovieReviewRepository movieReviewRepository;
    private final MovieRepository movieRepository;
    private final MovieReviewMapper movieReviewMapper;

    public MovieReviewService(MovieReviewRepository movieReviewRepository,
                              MovieRepository movieRepository,
                              MovieReviewMapper movieReviewMapper) {
        this.movieReviewRepository = movieReviewRepository;
        this.movieRepository = movieRepository;
        this.movieReviewMapper = movieReviewMapper;
    }

    @Transactional
    public ReviewResponse createReview(Long movieId, String username, ReviewRequest request) {
        if (!movieRepository.existsById(movieId)) {
            throw new MovieNotFoundException(movieId);
        }
        if (movieReviewRepository.existsByMovie_MovieIdAndUsername(movieId, username)) {
            throw new MovieReviewAlreadyExistsException(movieId, username);
        }
        Movie movie = movieRepository.getReferenceById(movieId);

        MovieReview review = movieReviewMapper.mapReview(movie, username, request);
        MovieReview saved = movieReviewRepository.save(review);
        return movieReviewMapper.mapReviewResponse(saved);
    }

    public ReviewPageResponse getMovieReviews(Long movieId, Integer page, Integer size) {
        if (!movieRepository.existsById(movieId)) {
            throw new MovieNotFoundException(movieId);
        }
        int resolvedSize = (size != null) ? size : pageSize;
        Pageable pageable = PageRequest.of(page, resolvedSize, Sort.by("createdAt").descending());
        Page<MovieReview> result = movieReviewRepository.findAllByMovie_MovieId(movieId, pageable);

        return movieReviewMapper.mapReviewPageResponse(result);
    }

    @Transactional
    public void deleteReview(Long movieId, Long reviewId) {
        if (!movieRepository.existsById(movieId)) {
            throw new MovieNotFoundException(movieId);
        }
        if (!movieReviewRepository.existsById(reviewId)) {
            throw new MovieReviewNotFoundException(reviewId);
        }
        movieReviewRepository.deleteByReviewIdAndMovie_MovieId(reviewId, movieId);
    }
}
