package com.nmaravic.movie.api.service;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.database.repository.MovieRepository;
import com.nmaravic.movie.api.exception.MovieAlreadyExistsException;
import com.nmaravic.movie.api.exception.MovieNotFoundException;
import com.nmaravic.movie.api.mapper.MovieMapper;
import com.nmaravic.movie.api.model.MoviePatchRequest;
import com.nmaravic.movie.api.model.MovieRequest;
import com.nmaravic.movie.api.model.MovieResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MovieCrudService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    public MovieCrudService(MovieRepository movieRepository,
                            MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
    }

    public MovieResponse createMovie(MovieRequest movieRequest) {
        validateMovie(movieRequest);
        Movie movie = movieMapper.mapMovie(movieRequest);
        return movieMapper.mapMovieResponse(movieRepository.save(movie));
    }

    private void validateMovie(MovieRequest request) {
        if (movieRepository.existsByTitleAndReleaseYear(request.getTitle(), request.getReleaseYear())) {
            throw new MovieAlreadyExistsException(request.getTitle(), request.getReleaseYear());
        }
    }

    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new MovieNotFoundException(id);
        }
        movieRepository.deleteById(id);
    }

    public MovieResponse replaceMovie(Long id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        
        movieMapper.mapMovie(request, movie);
        Movie saved = movieRepository.save(movie);
        return movieMapper.mapMovieResponse(saved);
    }

    public MovieResponse updateMovie(Long id, MoviePatchRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        
        movieMapper.mapMovie(request, movie);
        Movie saved = movieRepository.save(movie);
        return movieMapper.mapMovieResponse(saved);
    }
}
