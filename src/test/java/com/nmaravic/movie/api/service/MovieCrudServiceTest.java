package com.nmaravic.movie.api.service;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.database.repository.MovieRepository;
import com.nmaravic.movie.api.exception.MovieAlreadyExistsException;
import com.nmaravic.movie.api.exception.MovieNotFoundException;
import com.nmaravic.movie.api.mapper.MovieMapper;
import com.nmaravic.movie.api.model.Genre;
import com.nmaravic.movie.api.model.MoviePatchRequest;
import com.nmaravic.movie.api.model.MovieRequest;
import com.nmaravic.movie.api.model.MovieResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieCrudServiceTest {

    @InjectMocks
    private MovieCrudService movieCrudService;

    @Mock
    private MovieRepository movieRepository;

    @Spy
    private MovieMapper movieMapper;

    private Movie movie;
    private MovieRequest movieRequest;
    private MovieResponse movieResponse;

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setMovieId(1L);
        movie.setTitle("movie");
        movie.setOverview("overview");
        movie.setReleaseYear(2010);
        movie.setDirector("director");
        movie.setGenres(List.of(Genre.ACTION, Genre.SCI_FI));
        movie.setCreatedAt(LocalDateTime.now());
        movie.setUpdatedAt(LocalDateTime.now());

        movieRequest = new MovieRequest();
        movieRequest.setTitle("movie");
        movieRequest.setOverview("overview");
        movieRequest.setReleaseYear(2010);
        movieRequest.setDirector("director");
        movieRequest.setGenres(List.of(Genre.ACTION, Genre.SCI_FI));
        movie.setCreatedAt(LocalDateTime.now());
        movie.setUpdatedAt(LocalDateTime.now());

        movieResponse = new MovieResponse();
        movieResponse.setId(1L);
        movieResponse.setTitle("movie");
        movieResponse.setOverview("overview");
        movieResponse.setReleaseYear(2010);
        movieResponse.setDirector("director");
        movieResponse.setGenres(List.of(Genre.ACTION, Genre.SCI_FI));
        movie.setCreatedAt(LocalDateTime.now());
        movie.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateMovie_Success() {
        when(movieRepository.existsByTitleAndReleaseYear("movie", 2010)).thenReturn(false);
        when(movieMapper.mapMovie(movieRequest)).thenReturn(movie);
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        MovieResponse result = movieCrudService.createMovie(movieRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("movie");

        verify(movieRepository).existsByTitleAndReleaseYear("movie", 2010);
        verify(movieMapper).mapMovie(movieRequest);
        verify(movieRepository).save(movie);
        verify(movieMapper).mapMovieResponse(movie);
    }

    @Test
    void testCreateMovie_ThrowsMovieAlreadyExistsException_WhenDuplicate() {
        when(movieRepository.existsByTitleAndReleaseYear("movie", 2010)).thenReturn(true);

        assertThatThrownBy(() -> movieCrudService.createMovie(movieRequest))
                .isInstanceOf(MovieAlreadyExistsException.class)
                .hasMessageContaining("movie")
                .hasMessageContaining("2010");

        verify(movieRepository).existsByTitleAndReleaseYear("movie", 2010);
        verify(movieRepository, never()).save(any());
        verify(movieMapper, never()).mapMovie(any());
    }

    @Test
    void testDeleteMovie_Success() {
        when(movieRepository.existsById(1L)).thenReturn(true);

        movieCrudService.deleteMovie(1L);

        verify(movieRepository).existsById(1L);
        verify(movieRepository).deleteById(1L);
    }

    @Test
    void testDeleteMovie_ThrowsMovieNotFoundException_WhenNotFound() {
        when(movieRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> movieCrudService.deleteMovie(99L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");

        verify(movieRepository).existsById(99L);
        verify(movieRepository, never()).deleteById(any());
    }

    @Test
    void testReplaceMovie_Success() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        MovieResponse result = movieCrudService.replaceMovie(1L, movieRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("movie");

        verify(movieRepository).findById(1L);
        verify(movieRepository).save(movie);
        verify(movieMapper).mapMovieResponse(movie);
    }

    @Test
    void testReplaceMovie_ReplacesAllAttributes() {
        MovieRequest newRequest = new MovieRequest();
        newRequest.setTitle("Interstellar");
        newRequest.setOverview("Space odyssey");
        newRequest.setReleaseYear(2014);
        newRequest.setDirector("director");
        newRequest.setGenres(List.of(Genre.SCI_FI));

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        movieCrudService.replaceMovie(1L, newRequest);

        assertThat(movie.getTitle()).isEqualTo("Interstellar");
        assertThat(movie.getOverview()).isEqualTo("Space odyssey");
        assertThat(movie.getReleaseYear()).isEqualTo(2014);
        assertThat(movie.getDirector()).isEqualTo("director");
        assertThat(movie.getGenres()).isEqualTo(List.of(Genre.SCI_FI));
    }

    @Test
    void testReplaceMovie_ThrowsMovieNotFoundException_WhenNotFound() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieCrudService.replaceMovie(99L, movieRequest))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");

        verify(movieRepository).findById(99L);
        verify(movieRepository, never()).save(any());
    }

    @Test
    void testUpdateMovie_Success_UpdatesOnlyProvidedFields() {
        MoviePatchRequest patchRequest = new MoviePatchRequest();
        patchRequest.setTitle("Interstellar");

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        MovieResponse result = movieCrudService.updateMovie(1L, patchRequest);

        assertThat(result).isNotNull();
        assertThat(movie.getTitle()).isEqualTo("Interstellar");
        assertThat(movie.getOverview()).isEqualTo("overview");
        assertThat(movie.getReleaseYear()).isEqualTo(2010);
        assertThat(movie.getDirector()).isEqualTo("director");

        verify(movieRepository).findById(1L);
        verify(movieRepository).save(movie);
        verify(movieMapper).mapMovieResponse(movie);
    }

    @Test
    void testUpdateMovie_DoesNotUpdateNullFields() {
        MoviePatchRequest patchRequest = new MoviePatchRequest();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        movieCrudService.updateMovie(1L, patchRequest);

        assertThat(movie.getTitle()).isEqualTo("movie");
        assertThat(movie.getOverview()).isEqualTo("overview");
        assertThat(movie.getReleaseYear()).isEqualTo(2010);
        assertThat(movie.getDirector()).isEqualTo("director");
    }

    @Test
    void testUpdateMovie_UpdatesGenres_WhenNotEmpty() {
        MoviePatchRequest patchRequest = new MoviePatchRequest();
        patchRequest.setGenres(List.of(Genre.COMEDY));

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        movieCrudService.updateMovie(1L, patchRequest);

        assertThat(movie.getGenres()).isEqualTo(List.of(Genre.COMEDY));
    }

    @Test
    void testUpdateMovie_DoesNotUpdateGenres_WhenEmptyList() {
        MoviePatchRequest patchRequest = new MoviePatchRequest();
        patchRequest.setGenres(List.of());

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieMapper.mapMovieResponse(movie)).thenReturn(movieResponse);

        movieCrudService.updateMovie(1L, patchRequest);

        assertThat(movie.getGenres()).isEqualTo(List.of(Genre.ACTION, Genre.SCI_FI));

    }

    @Test
    void testUpdateMovie_ThrowsMovieNotFoundException_WhenNotFound() {
        MoviePatchRequest patchRequest = new MoviePatchRequest();
        patchRequest.setTitle("Interstellar");

        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieCrudService.updateMovie(99L, patchRequest))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");

        verify(movieRepository).findById(99L);
        verify(movieRepository, never()).save(any());
    }
}