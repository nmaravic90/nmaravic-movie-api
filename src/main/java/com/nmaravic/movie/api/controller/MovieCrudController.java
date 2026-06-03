package com.nmaravic.movie.api.controller;

import com.nmaravic.movie.api.MovieCrudApi;
import com.nmaravic.movie.api.model.MoviePatchRequest;
import com.nmaravic.movie.api.service.MovieCrudService;
import com.nmaravic.movie.api.model.MovieRequest;
import com.nmaravic.movie.api.model.MovieResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MovieCrudController implements MovieCrudApi {

    private final MovieCrudService movieCrudService;

    public MovieCrudController(MovieCrudService movieCrudService) {
        this.movieCrudService = movieCrudService;
    }

    @Override
    public ResponseEntity<MovieResponse> createMovie(MovieRequest movieRequest) {
        MovieResponse response = movieCrudService.createMovie(movieRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteMovie(Long id) {
        movieCrudService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<MovieResponse> replaceMovie(Long id, MovieRequest movieRequest) {
        MovieResponse response = movieCrudService.replaceMovie(id, movieRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<MovieResponse> updateMovie(Long id, MoviePatchRequest moviePatchRequest) {
        MovieResponse response = movieCrudService.updateMovie(id, moviePatchRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
