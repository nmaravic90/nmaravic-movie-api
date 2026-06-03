package com.nmaravic.movie.api.controller;

import com.nmaravic.movie.api.exception.MovieAlreadyExistsException;
import com.nmaravic.movie.api.exception.MovieNotFoundException;
import com.nmaravic.movie.api.model.Genre;
import com.nmaravic.movie.api.model.MoviePatchRequest;
import com.nmaravic.movie.api.model.MovieRequest;
import com.nmaravic.movie.api.model.MovieResponse;
import com.nmaravic.movie.api.service.MovieCrudService;
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
class MovieCrudControllerTest {

    @InjectMocks
    private MovieCrudController movieCrudController;

    @Mock
    private MovieCrudService movieCrudService;

    private MovieRequest movieRequest;
    private MovieResponse movieResponse;

    @BeforeEach
    void setUp() {
        movieRequest = new MovieRequest();
        movieRequest.setTitle("Inception");
        movieRequest.setOverview("A mind-bending thriller");
        movieRequest.setReleaseYear(2010);
        movieRequest.setDirector("Christopher Nolan");
        movieRequest.setGenres(List.of(Genre.ACTION));

        movieResponse = new MovieResponse();
        movieResponse.setId(1L);
        movieResponse.setTitle("Inception");
        movieResponse.setOverview("A mind-bending thriller");
        movieResponse.setReleaseYear(2010);
        movieResponse.setDirector("Christopher Nolan");
        movieResponse.setGenres(List.of(Genre.ACTION));
    }

    @Test
    void testCreateMovie_Returns201_WithBody() {
        when(movieCrudService.createMovie(movieRequest)).thenReturn(movieResponse);

        ResponseEntity<MovieResponse> result = movieCrudController.createMovie(movieRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(1L);
        assertThat(result.getBody().getTitle()).isEqualTo("Inception");

        verify(movieCrudService).createMovie(movieRequest);
    }

    @Test
    void testCreateMovie_MovieAlreadyExistsException() {
        when(movieCrudService.createMovie(movieRequest))
                .thenThrow(new MovieAlreadyExistsException("Inception", 2010));

        assertThatThrownBy(() -> movieCrudController.createMovie(movieRequest))
                .isInstanceOf(MovieAlreadyExistsException.class)
                .hasMessageContaining("Inception")
                .hasMessageContaining("2010");
    }

    @Test
    void testDeleteMovie_Returns204_NoContent() {
        doNothing().when(movieCrudService).deleteMovie(1L);

        ResponseEntity<Void> result = movieCrudController.deleteMovie(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();

        verify(movieCrudService).deleteMovie(1L);
    }

    @Test
    void testDeleteMovie_MovieNotFoundException() {
        doThrow(new MovieNotFoundException(99L)).when(movieCrudService).deleteMovie(99L);

        assertThatThrownBy(() -> movieCrudController.deleteMovie(99L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void testReplaceMovie_Returns200_WithBody() {
        when(movieCrudService.replaceMovie(1L, movieRequest)).thenReturn(movieResponse);

        ResponseEntity<MovieResponse> result = movieCrudController.replaceMovie(1L, movieRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(1L);
        assertThat(result.getBody().getTitle()).isEqualTo("Inception");

        verify(movieCrudService).replaceMovie(1L, movieRequest);
    }

    @Test
    void testReplaceMovie_MovieNotFoundException() {
        when(movieCrudService.replaceMovie(99L, movieRequest))
                .thenThrow(new MovieNotFoundException(99L));

        assertThatThrownBy(() -> movieCrudController.replaceMovie(99L, movieRequest))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void testUpdateMovie_Returns200_WithBody() {
        MoviePatchRequest patchRequest = new MoviePatchRequest();
        patchRequest.setTitle("Interstellar");

        when(movieCrudService.updateMovie(1L, patchRequest)).thenReturn(movieResponse);

        ResponseEntity<MovieResponse> result = movieCrudController.updateMovie(1L, patchRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(1L);

        verify(movieCrudService).updateMovie(1L, patchRequest);
    }

    @Test
    void testUpdateMovie_MovieNotFoundException() {
        MoviePatchRequest patchRequest = new MoviePatchRequest();
        patchRequest.setTitle("Interstellar");

        when(movieCrudService.updateMovie(99L, patchRequest))
                .thenThrow(new MovieNotFoundException(99L));

        assertThatThrownBy(() -> movieCrudController.updateMovie(99L, patchRequest))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");
    }
}