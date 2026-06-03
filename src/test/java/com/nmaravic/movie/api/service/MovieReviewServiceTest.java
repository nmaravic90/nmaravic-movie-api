package com.nmaravic.movie.api.service;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.database.entitymodel.MovieReview;
import com.nmaravic.movie.api.database.repository.MovieRepository;
import com.nmaravic.movie.api.database.repository.MovieReviewRepository;
import com.nmaravic.movie.api.exception.MovieNotFoundException;
import com.nmaravic.movie.api.exception.MovieReviewAlreadyExistsException;
import com.nmaravic.movie.api.exception.MovieReviewNotFoundException;
import com.nmaravic.movie.api.mapper.MovieReviewMapper;
import com.nmaravic.movie.api.model.ReviewPageResponse;
import com.nmaravic.movie.api.model.ReviewRequest;
import com.nmaravic.movie.api.model.ReviewResponse;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieReviewServiceTest {

    @InjectMocks
    private MovieReviewService movieReviewService;

    @Mock
    private MovieReviewRepository movieReviewRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieReviewMapper movieReviewMapper;

    private Movie movie;
    private MovieReview movieReview;
    private ReviewRequest reviewRequest;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(movieReviewService, "pageSize", 10);

        movie = new Movie();
        movie.setMovieId(1L);
        movie.setTitle("Inception");

        movieReview = new MovieReview();
        movieReview.setReviewId(1L);
        movieReview.setMovie(movie);
        movieReview.setUsername("testuser");
        movieReview.setRating(8);
        movieReview.setComment("Great movie!");

        reviewRequest = new ReviewRequest();
        reviewRequest.setRating(8);
        reviewRequest.setComment("Great movie!");

        reviewResponse = new ReviewResponse();
        reviewResponse.setId(1L);
        reviewResponse.setMovieId(1L);
        reviewResponse.setUsername("testuser");
        reviewResponse.setRating(8);
        reviewResponse.setComment("Great movie!");
    }

    @Test
    void testCreateReview_Success() {
        when(movieRepository.existsById(1L)).thenReturn(true);
        when(movieReviewRepository.existsByMovie_MovieIdAndUsername(1L, "testuser")).thenReturn(false);
        when(movieRepository.getReferenceById(1L)).thenReturn(movie);
        when(movieReviewMapper.mapReview(movie, "testuser", reviewRequest)).thenReturn(movieReview);
        when(movieReviewRepository.save(movieReview)).thenReturn(movieReview);
        when(movieReviewMapper.mapReviewResponse(movieReview)).thenReturn(reviewResponse);

        ReviewResponse result = movieReviewService.createReview(1L, "testuser", reviewRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getRating()).isEqualTo(8);

        verify(movieRepository).existsById(1L);
        verify(movieReviewRepository).existsByMovie_MovieIdAndUsername(1L, "testuser");
        verify(movieRepository).getReferenceById(1L);
        verify(movieReviewMapper).mapReview(movie, "testuser", reviewRequest);
        verify(movieReviewRepository).save(movieReview);
        verify(movieReviewMapper).mapReviewResponse(movieReview);
    }

    @Test
    void testCreateReview_ThrowsMovieNotFoundException_WhenMovieNotFound() {
        when(movieRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> movieReviewService.createReview(99L, "testuser", reviewRequest))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");

        verify(movieRepository).existsById(99L);
        verify(movieReviewRepository, never()).save(any());
    }

    @Test
    void testCreateReview_ThrowsMovieReviewAlreadyExistsException_WhenDuplicate() {
        when(movieRepository.existsById(1L)).thenReturn(true);
        when(movieReviewRepository.existsByMovie_MovieIdAndUsername(1L, "testuser")).thenReturn(true);

        assertThatThrownBy(() -> movieReviewService.createReview(1L, "testuser", reviewRequest))
                .isInstanceOf(MovieReviewAlreadyExistsException.class)
                .hasMessageContaining("testuser");

        verify(movieReviewRepository, never()).save(any());
    }

    @Test
    void testGetMovieReviews_Success_WithExplicitSize() {
        Page<MovieReview> page = new PageImpl<>(List.of(movieReview), PageRequest.of(0, 5), 1);
        ReviewPageResponse pageResponse = new ReviewPageResponse();
        pageResponse.setContent(List.of(reviewResponse));
        pageResponse.setPage(0);
        pageResponse.setSize(5);
        pageResponse.setTotalElements(1L);
        pageResponse.setTotalPages(1);

        when(movieRepository.existsById(1L)).thenReturn(true);
        when(movieReviewRepository.findAllByMovie_MovieId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(movieReviewMapper.mapReviewPageResponse(page)).thenReturn(pageResponse);

        ReviewPageResponse result = movieReviewService.getMovieReviews(1L, 0, 5);

        assertThat(result).isNotNull();
        assertThat(result.getPage()).isZero();
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent()).hasSize(1);

        verify(movieRepository).existsById(1L);
        verify(movieReviewRepository).findAllByMovie_MovieId(eq(1L), any(Pageable.class));
        verify(movieReviewMapper).mapReviewPageResponse(page);
    }

    @Test
    void testGetMovieReviews_UsesDefaultPageSize_WhenSizeIsNull() {
        Page<MovieReview> page = new PageImpl<>(List.of(movieReview), PageRequest.of(0, 10), 1);
        ReviewPageResponse pageResponse = new ReviewPageResponse();
        pageResponse.setContent(List.of(reviewResponse));
        pageResponse.setPage(0);
        pageResponse.setSize(10);
        pageResponse.setTotalElements(1L);
        pageResponse.setTotalPages(1);

        when(movieRepository.existsById(1L)).thenReturn(true);
        when(movieReviewRepository.findAllByMovie_MovieId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(movieReviewMapper.mapReviewPageResponse(page)).thenReturn(pageResponse);

        ReviewPageResponse result = movieReviewService.getMovieReviews(1L, 0, null);

        assertThat(result).isNotNull();
        assertThat(result.getSize()).isEqualTo(10); // default pageSize
    }

    @Test
    void testGetMovieReviews_ThrowsMovieNotFoundException_WhenMovieNotFound() {
        when(movieRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> movieReviewService.getMovieReviews(99L, 0, 10))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");

        verify(movieReviewRepository, never()).findAllByMovie_MovieId(any(), any());
    }

    @Test
    void testDeleteReview_Success() {
        when(movieRepository.existsById(1L)).thenReturn(true);
        when(movieReviewRepository.existsById(1L)).thenReturn(true);

        movieReviewService.deleteReview(1L, 1L);

        verify(movieRepository).existsById(1L);
        verify(movieReviewRepository).existsById(1L);
        verify(movieReviewRepository).deleteByReviewIdAndMovie_MovieId(1L, 1L);
    }

    @Test
    void testDeleteReview_ThrowsMovieNotFoundException_WhenMovieNotFound() {
        when(movieRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> movieReviewService.deleteReview(99L, 1L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");

        verify(movieReviewRepository, never()).deleteByReviewIdAndMovie_MovieId(any(), any());
    }

    @Test
    void testDeleteReview_ThrowsMovieReviewNotFoundException_WhenReviewNotFound() {
        when(movieRepository.existsById(1L)).thenReturn(true);
        when(movieReviewRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> movieReviewService.deleteReview(1L, 99L))
                .isInstanceOf(MovieReviewNotFoundException.class)
                .hasMessageContaining("99");

        verify(movieReviewRepository, never()).deleteByReviewIdAndMovie_MovieId(any(), any());
    }
}