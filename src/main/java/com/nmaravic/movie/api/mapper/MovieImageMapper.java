package com.nmaravic.movie.api.mapper;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.database.entitymodel.MovieImage;
import com.nmaravic.movie.api.model.ImageType;
import com.nmaravic.movie.api.model.MovieImageResponse;
import org.springframework.stereotype.Component;

@Component
public class MovieImageMapper {

    public MovieImage mapMovieImage(Movie movie, String url, ImageType type) {
        MovieImage movieImage = new MovieImage();
        movieImage.setMovie(movie);
        movieImage.setUrl(url);
        movieImage.setType(type);
        return movieImage;
    }

    public MovieImage mapMovieImage(MovieImageResponse movieImageResponse){
        MovieImage movieImage = new MovieImage();
        movieImage.setImageId(movieImageResponse.getId());
        movieImage.setType(movieImageResponse.getType());
        movieImage.setUrl(movieImageResponse.getUrl());
        return movieImage;
    }

    public MovieImageResponse mapMovieImageResponse(MovieImage movieImage) {
        return new MovieImageResponse()
                .id(movieImage.getImageId())
                .type(movieImage.getType())
                .url(movieImage.getUrl())
                .createdAt(movieImage.getCreatedAt().toLocalDate());
    }
}
