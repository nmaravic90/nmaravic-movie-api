package com.nmaravic.movie.api.controller;

import com.nmaravic.movie.api.exception.MovieNotFoundException;
import com.nmaravic.movie.api.model.Genre;
import com.nmaravic.movie.api.model.MoviePageResponse;
import com.nmaravic.movie.api.model.MovieResponse;
import com.nmaravic.movie.api.service.MovieReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieReadControllerTest {

    @Mock
    private MovieReadService movieReadService;

    @InjectMocks
    private MovieReadController movieReadController;

    private MovieResponse movieResponse;

    @BeforeEach
    void setUp() {
        movieResponse = new MovieResponse();
        movieResponse.setId(1L);
        movieResponse.setTitle("Inception");
        movieResponse.setOverview("A mind-bending thriller");
        movieResponse.setReleaseYear(2010);
        movieResponse.setDirector("Christopher Nolan");
        movieResponse.setGenres(List.of(Genre.ACTION));
        movieResponse.setAvgRating(8.8);
        movieResponse.setRatingCount(100);
    }

    @Test
    void testGetMovieById_Returns200_WithBody() {
        when(movieReadService.getMovieById(1L)).thenReturn(movieResponse);

        ResponseEntity<MovieResponse> result = movieReadController.getMovieById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(1L);
        assertThat(result.getBody().getTitle()).isEqualTo("Inception");

        verify(movieReadService).getMovieById(1L);
    }

    @Test
    void testGetMovieById_PropagatesMovieNotFoundException() {
        when(movieReadService.getMovieById(99L)).thenThrow(new MovieNotFoundException(99L));

        assertThatThrownBy(() -> movieReadController.getMovieById(99L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void testGetMovies_Returns200_WithPageResponse() {
        MoviePageResponse pageResponse = new MoviePageResponse();
        pageResponse.setContent(List.of(movieResponse));
        pageResponse.setPage(0);
        pageResponse.setSize(10);
        pageResponse.setTotalElements(1L);
        pageResponse.setTotalPages(1);

        when(movieReadService.getMovies(null, null, null, null, "title", "asc", 0, 10))
                .thenReturn(pageResponse);

        ResponseEntity<MoviePageResponse> result = movieReadController.getMovies(
                null, null, null, null, "title", "asc", 0, 10);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getPage()).isZero();
        assertThat(result.getBody().getSize()).isEqualTo(10);
        assertThat(result.getBody().getTotalElements()).isEqualTo(1L);
        assertThat(result.getBody().getTotalPages()).isEqualTo(1);
        assertThat(result.getBody().getContent()).hasSize(1);

        verify(movieReadService).getMovies(null, null, null, null, "title", "asc", 0, 10);
    }

    @Test
    void testGetMovies_WithFilters_Returns200() {
        MoviePageResponse pageResponse = new MoviePageResponse();
        pageResponse.setContent(List.of(movieResponse));
        pageResponse.setPage(0);
        pageResponse.setSize(5);
        pageResponse.setTotalElements(1L);
        pageResponse.setTotalPages(1);

        when(movieReadService.getMovies("Inception", Genre.ACTION, 2010, 8.0, "rating", "desc", 0, 5))
                .thenReturn(pageResponse);

        ResponseEntity<MoviePageResponse> result = movieReadController.getMovies(
                "Inception", Genre.ACTION, 2010, 8.0, "rating", "desc", 0, 5);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).hasSize(1);

        verify(movieReadService).getMovies("Inception", Genre.ACTION, 2010, 8.0, "rating", "desc", 0, 5);
    }

    @Test
    void testGetMovies_ReturnsEmptyPage_WhenNoResults() {
        MoviePageResponse emptyPage = new MoviePageResponse();
        emptyPage.setContent(List.of());
        emptyPage.setPage(0);
        emptyPage.setSize(10);
        emptyPage.setTotalElements(0L);
        emptyPage.setTotalPages(0);

        when(movieReadService.getMovies("nonexistent", null, null, null, "title", "asc", 0, 10))
                .thenReturn(emptyPage);

        ResponseEntity<MoviePageResponse> result = movieReadController.getMovies(
                "nonexistent", null, null, null, "title", "asc", 0, 10);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).isEmpty();
        assertThat(result.getBody().getTotalElements()).isZero();
    }

    @Test
    void testGetTopRatedMovies_Returns200_WithList() {
        MovieResponse second = new MovieResponse();
        second.setId(2L);
        second.setTitle("The Dark Knight");
        second.setAvgRating(9.0);

        List<MovieResponse> topRated = List.of(movieResponse, second);
        when(movieReadService.getTopRatedMovies(2)).thenReturn(topRated);

        ResponseEntity<List<MovieResponse>> result = movieReadController.getTopRatedMovies(2);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).hasSize(2);
        assertThat(result.getBody().get(0).getId()).isEqualTo(1L);
        assertThat(result.getBody().get(1).getId()).isEqualTo(2L);

        verify(movieReadService).getTopRatedMovies(2);
    }

    @Test
    void testGetTopRatedMovies_ReturnsEmptyList_WhenNoMovies() {
        when(movieReadService.getTopRatedMovies(10)).thenReturn(List.of());

        ResponseEntity<List<MovieResponse>> result = movieReadController.getTopRatedMovies(10);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).isEmpty();
    }
}