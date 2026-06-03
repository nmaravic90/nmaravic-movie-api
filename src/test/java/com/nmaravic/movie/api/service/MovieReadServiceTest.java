package com.nmaravic.movie.api.service;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.database.repository.MovieRepository;
import com.nmaravic.movie.api.exception.MovieNotFoundException;
import com.nmaravic.movie.api.mapper.MovieMapper;
import com.nmaravic.movie.api.model.Genre;
import com.nmaravic.movie.api.model.MoviePageResponse;
import com.nmaravic.movie.api.model.MovieResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieReadServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieReadService movieReadService;

    private Movie movie;
    private MovieResponse movieResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(movieReadService, "pageSize", 10);
        ReflectionTestUtils.setField(movieReadService, "topRatedLimit", 5);

        movie = new Movie();
        movie.setMovieId(1L);
        movie.setTitle("Inception");
        movie.setOverview("A mind-bending thriller");
        movie.setReleaseYear(2010);
        movie.setDirector("Christopher Nolan");
        movie.setAvgRating(8.8);
        movie.setGenres(List.of(Genre.ACTION));

        movieResponse = new MovieResponse();
        movieResponse.setId(1L);
        movieResponse.setTitle("Inception");
        movieResponse.setOverview("A mind-bending thriller");
        movieResponse.setReleaseYear(2010);
        movieResponse.setDirector("Christopher Nolan");
        movieResponse.setAvgRating(8.8);
        movieResponse.setGenres(List.of(Genre.ACTION));
    }

    @Test
    void testGetMovieById_Success() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        MovieResponse result = movieReadService.getMovieById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Inception");

        verify(movieRepository).findById(1L);
        verify(movieMapper).mapMovieResponse(movie);
    }

    @Test
    void testGetMovieById_ThrowsMovieNotFoundException_WhenNotFound() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieReadService.getMovieById(99L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");

        verify(movieRepository).findById(99L);
        verify(movieMapper, never()).mapMovieResponse(any());
    }

    @Test
    void testGetMovies_Success_WithNoFilters() {
        Page<Movie> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 10), 1);

        when(movieRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        MoviePageResponse result = movieReadService.getMovies(null, null, null, null, "title", "asc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getPage()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Inception");

        verify(movieRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(movieMapper).mapMovieResponse(movie);
    }

    @Test
    void testGetMovies_Success_WithAllFilters() {
        Page<Movie> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 5), 1);

        when(movieRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        MoviePageResponse result = movieReadService.getMovies(
                "Inception", Genre.ACTION, 2010, 8.0, "avgRating", "desc", 0, 5);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(movieRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testGetMovies_UsesDefaultPageSize_WhenSizeIsNull() {
        Page<Movie> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 10), 1);

        when(movieRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        MoviePageResponse result = movieReadService.getMovies(null, null, null, null, "title", "asc", 0, null);

        assertThat(result).isNotNull();
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    void testGetMovies_ReturnsEmptyPage_WhenNoResults() {
        Page<Movie> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(movieRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        MoviePageResponse result = movieReadService.getMovies(
                "nonexistent", null, null, null, "title", "asc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(movieMapper, never()).mapMovieResponse(any());
    }

    @Test
    void testGetMovies_SortsDescending_WhenOrderIsDesc() {
        Page<Movie> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 10), 1);

        when(movieRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        MoviePageResponse result = movieReadService.getMovies(
                null, null, null, null, "avgRating", "desc", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(movieRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testGetTopRatedMovies_Success_WithExplicitLimit() {
        Movie second = new Movie();
        second.setMovieId(2L);
        second.setTitle("The Dark Knight");
        second.setAvgRating(9.0);

        MovieResponse secondResponse = new MovieResponse();
        secondResponse.setId(2L);
        secondResponse.setTitle("The Dark Knight");
        secondResponse.setAvgRating(9.0);

        when(movieRepository.findAllByOrderByAvgRatingDesc(any(Pageable.class)))
                .thenReturn(List.of(second, movie));
        when(movieMapper.mapMovieResponse(second)).thenReturn(secondResponse);
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        List<MovieResponse> result = movieReadService.getTopRatedMovies(2);

        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(1L);
        verify(movieRepository).findAllByOrderByAvgRatingDesc(any(Pageable.class));
    }

    @Test
    void testGetTopRatedMovies_UsesDefaultLimit_WhenLimitIsNull() {
        when(movieRepository.findAllByOrderByAvgRatingDesc(any(Pageable.class)))
                .thenReturn(List.of(movie));
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        List<MovieResponse> result = movieReadService.getTopRatedMovies(null);

        assertThat(result).isNotNull().hasSize(1);
        verify(movieRepository).findAllByOrderByAvgRatingDesc(argThat(pageable ->
                pageable.getPageSize() == 5));
    }

    @Test
    void testGetTopRatedMovies_ReturnsEmptyList_WhenNoMovies() {
        when(movieRepository.findAllByOrderByAvgRatingDesc(any(Pageable.class)))
                .thenReturn(List.of());

        List<MovieResponse> result = movieReadService.getTopRatedMovies(10);

        assertThat(result).isNotNull().isEmpty();
        verify(movieMapper, never()).mapMovieResponse(any());
    }
}