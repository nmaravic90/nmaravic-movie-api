package com.nmaravic.movie.api.controller;

import com.nmaravic.movie.api.MovieReadApi;
import com.nmaravic.movie.api.model.Genre;
import com.nmaravic.movie.api.model.MoviePageResponse;
import com.nmaravic.movie.api.model.MovieResponse;
import com.nmaravic.movie.api.service.MovieReadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MovieReadController implements MovieReadApi {

    private final MovieReadService movieReadService;

    public MovieReadController(MovieReadService movieReadService) {
        this.movieReadService = movieReadService;
    }

    @Override
    public ResponseEntity<MovieResponse> getMovieById(Long id) {
        MovieResponse movieResponse = movieReadService.getMovieById(id);
        return new ResponseEntity<>(movieResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<MoviePageResponse> getMovies(String query, Genre genre, Integer year, Double minRating,
            String sort, String order, Integer page, Integer size) {
        MoviePageResponse response = movieReadService.getMovies(query, genre, year, minRating, sort, order, page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<MovieResponse>> getTopRatedMovies(Integer limit) {
        List<MovieResponse> response = movieReadService.getTopRatedMovies(limit);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
