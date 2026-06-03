
package com.nmaravic.movie.api.controller;

import com.nmaravic.movie.api.exception.MovieNotFoundException;
import com.nmaravic.movie.api.exception.MovieReviewAlreadyExistsException;
import com.nmaravic.movie.api.exception.MovieReviewNotFoundException;
import com.nmaravic.movie.api.model.ReviewPageResponse;
import com.nmaravic.movie.api.model.ReviewRequest;
import com.nmaravic.movie.api.model.ReviewResponse;
import com.nmaravic.movie.api.service.MovieReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieReviewControllerTest {

    @InjectMocks
    private MovieReviewController movieReviewController;

    @Mock
    private MovieReviewService movieReviewService;

    @Mock
    private SecurityContext securityContext;

    private ReviewRequest reviewRequest;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
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

    private void mockSecurityContext(String username) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("preferred_username")).thenReturn(username);

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);
        when(authentication.getToken()).thenReturn(jwt);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockAnonymousSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testCreateReview_Returns201_WithBody() {
        mockSecurityContext("testuser");
        when(movieReviewService.createReview(1L, "testuser", reviewRequest)).thenReturn(reviewResponse);

        ResponseEntity<ReviewResponse> result = movieReviewController.createReview(1L, reviewRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(1L);
        assertThat(result.getBody().getUsername()).isEqualTo("testuser");
        assertThat(result.getBody().getRating()).isEqualTo(8);

        verify(movieReviewService).createReview(1L, "testuser", reviewRequest);
    }

    @Test
    void testCreateReview_UsesAnonymous_WhenNoAuthentication() {
        mockAnonymousSecurityContext();
        when(movieReviewService.createReview(1L, "anonymous", reviewRequest)).thenReturn(reviewResponse);

        ResponseEntity<ReviewResponse> result = movieReviewController.createReview(1L, reviewRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(movieReviewService).createReview(1L, "anonymous", reviewRequest);
    }

    @Test
    void testCreateReview_PropagatesMovieNotFoundException() {
        mockSecurityContext("testuser");
        when(movieReviewService.createReview(99L, "testuser", reviewRequest))
                .thenThrow(new MovieNotFoundException(99L));

        assertThatThrownBy(() -> movieReviewController.createReview(99L, reviewRequest))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void testCreateReview_PropagatesMovieReviewAlreadyExistsException() {
        mockSecurityContext("testuser");
        when(movieReviewService.createReview(1L, "testuser", reviewRequest))
                .thenThrow(new MovieReviewAlreadyExistsException(1L, "testuser"));

        assertThatThrownBy(() -> movieReviewController.createReview(1L, reviewRequest))
                .isInstanceOf(MovieReviewAlreadyExistsException.class)
                .hasMessageContaining("testuser");
    }

    @Test
    void testGetMovieReviews_Returns200_WithPageResponse() {
        ReviewPageResponse pageResponse = new ReviewPageResponse();
        pageResponse.setContent(List.of(reviewResponse));
        pageResponse.setPage(0);
        pageResponse.setSize(10);
        pageResponse.setTotalElements(1L);
        pageResponse.setTotalPages(1);

        when(movieReviewService.getMovieReviews(1L, 0, 10)).thenReturn(pageResponse);

        ResponseEntity<ReviewPageResponse> result = movieReviewController.getMovieReviews(1L, 0, 10);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getPage()).isZero();
        assertThat(result.getBody().getSize()).isEqualTo(10);
        assertThat(result.getBody().getTotalElements()).isEqualTo(1L);
        assertThat(result.getBody().getContent()).hasSize(1);

        verify(movieReviewService).getMovieReviews(1L, 0, 10);
    }

    @Test
    void testGetMovieReviews_PropagatesMovieNotFoundException() {
        when(movieReviewService.getMovieReviews(99L, 0, 10))
                .thenThrow(new MovieNotFoundException(99L));

        assertThatThrownBy(() -> movieReviewController.getMovieReviews(99L, 0, 10))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void testDeleteReview_Returns204_NoContent() {
        doNothing().when(movieReviewService).deleteReview(1L, 1L);

        ResponseEntity<Void> result = movieReviewController.deleteReview(1L, 1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();

        verify(movieReviewService).deleteReview(1L, 1L);
    }

    @Test
    void testDeleteReview_PropagatesMovieNotFoundException() {
        doThrow(new MovieNotFoundException(99L)).when(movieReviewService).deleteReview(99L, 1L);

        assertThatThrownBy(() -> movieReviewController.deleteReview(99L, 1L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void testDeleteReview_PropagatesMovieReviewNotFoundException() {
        doThrow(new MovieReviewNotFoundException(99L)).when(movieReviewService).deleteReview(1L, 99L);

        assertThatThrownBy(() -> movieReviewController.deleteReview(1L, 99L))
                .isInstanceOf(MovieReviewNotFoundException.class)
                .hasMessageContaining("99");
    }
}