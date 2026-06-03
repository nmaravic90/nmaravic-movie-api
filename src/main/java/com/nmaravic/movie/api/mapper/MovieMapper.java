package com.nmaravic.movie.api.mapper;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.model.MoviePatchRequest;
import com.nmaravic.movie.api.model.MovieRequest;
import com.nmaravic.movie.api.model.MovieResponse;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

    public Movie mapMovie(MovieRequest movieRequest) {
        Movie movie = new Movie();
        movie.setTitle(movieRequest.getTitle());
        movie.setOverview(movieRequest.getOverview());
        movie.setReleaseYear(movieRequest.getReleaseYear());
        movie.setDirector(movieRequest.getDirector());
        movie.setGenres(movieRequest.getGenres());
        return movie;
    }

    public MovieResponse mapMovieResponse(Movie movie) {
        return new MovieResponse()
                .id(movie.getMovieId())
                .title(movie.getTitle())
                .overview(movie.getOverview())
                .releaseYear(movie.getReleaseYear())
                .director(movie.getDirector())
                .genres(movie.getGenres())
                .avgRating(movie.getAvgRating())
                .ratingCount(movie.getRatingCount())
                .createdAt(movie.getCreatedAt().toLocalDate())
                .updatedAt(movie.getUpdatedAt().toLocalDate());
    }

    public void mapMovie(MovieRequest request, Movie movie) {
        movie.setTitle(request.getTitle());
        movie.setOverview(request.getOverview());
        movie.setReleaseYear(request.getReleaseYear());
        movie.setDirector(request.getDirector());
        movie.setGenres(request.getGenres());
    }

    public void mapMovie(MoviePatchRequest request, Movie movie) {
        if (request.getTitle() != null) movie.setTitle(request.getTitle());
        if (request.getOverview() != null) movie.setOverview(request.getOverview());
        if (request.getReleaseYear() != null) movie.setReleaseYear(request.getReleaseYear());
        if (request.getDirector() != null) movie.setDirector(request.getDirector());
        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            movie.setGenres(request.getGenres());
        }
    }
}
