package com.nmaravic.movie.api.controller;

import com.nmaravic.movie.api.MovieImageApi;
import com.nmaravic.movie.api.model.ImageType;
import com.nmaravic.movie.api.model.MovieImageResponse;
import com.nmaravic.movie.api.service.MovieImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class MovieImageController implements MovieImageApi {

    private final MovieImageService movieImageService;

    public MovieImageController(MovieImageService movieImageService) {
        this.movieImageService = movieImageService;
    }

    @Override
    public ResponseEntity<MovieImageResponse> uploadMovieImage(Long movieId, MultipartFile file, ImageType type) {
        MovieImageResponse movieImageResponse = movieImageService.uploadMovieImage(movieId, file, type);
        return new ResponseEntity<>(movieImageResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteAllMovieImages(Long movieId) {
        movieImageService.deleteAllMovieImages(movieId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<MovieImageResponse>> getMovieImages(Long movieId) {
        List<MovieImageResponse> movieImages = movieImageService.getMovieImages(movieId);
        return new ResponseEntity<>(movieImages, HttpStatus.OK);
    }
}