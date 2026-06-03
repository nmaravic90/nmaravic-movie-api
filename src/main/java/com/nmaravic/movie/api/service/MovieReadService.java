package com.nmaravic.movie.api.service;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.database.repository.MovieRepository;
import com.nmaravic.movie.api.exception.MovieNotFoundException;
import com.nmaravic.movie.api.mapper.MovieMapper;
import com.nmaravic.movie.api.model.Genre;
import com.nmaravic.movie.api.model.MoviePageResponse;
import com.nmaravic.movie.api.model.MovieResponse;
import com.nmaravic.movie.api.util.MovieSpecificationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieReadService {

    private static final String SORT_DESC = "desc";

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    @Value("${movie.page.size}")
    private int pageSize;

    @Value("${movie.top.rated.limit}")
    private int topRatedLimit;

    public MovieReadService(MovieRepository movieRepository,
                            MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        return movieMapper.mapMovieResponse(movie);
    }

    @SuppressWarnings("java:S107")
    public MoviePageResponse getMovies(String query, Genre genre, Integer year, Double minRating,
                                       String sort, String order, Integer page, Integer size) {
        Specification<Movie> spec = buildSpecification(query, genre, year, minRating);
        Pageable pageable = buildPageable(page, size, sort, order);
        Page<Movie> result = movieRepository.findAll(spec, pageable);
        return createMoviePageResponse(result);
    }

    public List<MovieResponse> getTopRatedMovies(Integer limit) {
        Pageable pageable = PageRequest.of(0, getTopRatedPageSize(limit));
        return movieRepository.findAllByOrderByAvgRatingDesc(pageable)
                .stream()
                .map(movieMapper::mapMovieResponse)
                .toList();
    }

    private Specification<Movie> buildSpecification(String query, Genre genre, Integer year, Double minRating) {
        return Specification
                .where(MovieSpecificationUtil.hasQuery(query))
                .and(MovieSpecificationUtil.hasGenre(genre))
                .and(MovieSpecificationUtil.hasYear(year))
                .and(MovieSpecificationUtil.hasMinRating(minRating));
    }

    private Pageable buildPageable(Integer page, Integer size, String sort, String order) {
        Sort sorting = order.equalsIgnoreCase(SORT_DESC) ?
                Sort.by(sort).descending() : Sort.by(sort).ascending();
        return PageRequest.of(page, getPageSize(size), sorting);
    }

    private int getPageSize(Integer size){
        return (size != null) ? size : pageSize;
    }

    private MoviePageResponse createMoviePageResponse(Page<Movie> result){
        return new MoviePageResponse()
                .content(result.getContent().stream()
                        .map(movieMapper::mapMovieResponse)
                        .toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages());
    }

    private int getTopRatedPageSize(Integer limit) {
        return (limit != null) ? limit : topRatedLimit;
    }
}
