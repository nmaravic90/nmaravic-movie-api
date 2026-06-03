package com.nmaravic.movie.api.controller;

import com.nmaravic.movie.api.exception.MovieImageLimitException;
import com.nmaravic.movie.api.exception.MovieNotFoundException;
import com.nmaravic.movie.api.model.ImageType;
import com.nmaravic.movie.api.model.MovieImageResponse;
import com.nmaravic.movie.api.service.MovieImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieImageControllerTest {

    @InjectMocks
    private MovieImageController movieImageController;

    @Mock
    private MovieImageService movieImageService;

    private MultipartFile mockFile;
    private MovieImageResponse imageResponse;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
                "file",
                "cover.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        imageResponse = new MovieImageResponse();
        imageResponse.setId(1L);
        imageResponse.setUrl("http://localhost/1/cover.jpg");
        imageResponse.setType(ImageType.COVER);
    }

    @Test
    void testUploadMovieImage_Returns201_WithBody() {
        when(movieImageService.uploadMovieImage(1L, mockFile, ImageType.COVER)).thenReturn(imageResponse);

        ResponseEntity<MovieImageResponse> result = movieImageController.uploadMovieImage(1L, mockFile, ImageType.COVER);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(1L);
        assertThat(result.getBody().getUrl()).isEqualTo("http://localhost/1/cover.jpg");
        assertThat(result.getBody().getType()).isEqualTo(ImageType.COVER);

        verify(movieImageService).uploadMovieImage(1L, mockFile, ImageType.COVER);
    }

    @Test
    void testUploadMovieImage_PropagatesMovieNotFoundException() {
        when(movieImageService.uploadMovieImage(99L, mockFile, ImageType.COVER))
                .thenThrow(new MovieNotFoundException(99L));

        assertThatThrownBy(() -> movieImageController.uploadMovieImage(99L, mockFile, ImageType.COVER))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void testUploadMovieImage_PropagatesMovieImageLimitException_WhenCoverAlreadyExists() {
        when(movieImageService.uploadMovieImage(1L, mockFile, ImageType.COVER))
                .thenThrow(new MovieImageLimitException("Cover image already exists for movie 1"));

        assertThatThrownBy(() -> movieImageController.uploadMovieImage(1L, mockFile, ImageType.COVER))
                .isInstanceOf(MovieImageLimitException.class)
                .hasMessageContaining("Cover image already exists");
    }

    @Test
    void testUploadMovieImage_PropagatesMovieImageLimitException_WhenSlideLimit() {
        MultipartFile slideFile = new MockMultipartFile(
                "file", "slide.jpg", "image/jpeg", "fake".getBytes()
        );

        when(movieImageService.uploadMovieImage(1L, slideFile, ImageType.SLIDE))
                .thenThrow(new MovieImageLimitException("Maximum slide limit (3) reached for movie 1"));

        assertThatThrownBy(() -> movieImageController.uploadMovieImage(1L, slideFile, ImageType.SLIDE))
                .isInstanceOf(MovieImageLimitException.class)
                .hasMessageContaining("Maximum slide limit");
    }

    @Test
    void testDeleteAllMovieImages_Returns204_NoContent() {
        doNothing().when(movieImageService).deleteAllMovieImages(1L);

        ResponseEntity<Void> result = movieImageController.deleteAllMovieImages(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();

        verify(movieImageService).deleteAllMovieImages(1L);
    }

    @Test
    void testDeleteAllMovieImages_PropagatesMovieNotFoundException() {
        doThrow(new MovieNotFoundException(99L)).when(movieImageService).deleteAllMovieImages(99L);

        assertThatThrownBy(() -> movieImageController.deleteAllMovieImages(99L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void testGetMovieImages_Returns201_WithBody() {
        MovieImageResponse slideResponse = new MovieImageResponse();
        slideResponse.setId(2L);
        slideResponse.setUrl("http://localhost/1/slide1.jpg");
        slideResponse.setType(ImageType.SLIDE);

        List<MovieImageResponse> images = List.of(imageResponse, slideResponse);
        when(movieImageService.getMovieImages(1L)).thenReturn(images);

        ResponseEntity<List<MovieImageResponse>> result = movieImageController.getMovieImages(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).hasSize(2);
        assertThat(result.getBody().get(0).getId()).isEqualTo(1L);
        assertThat(result.getBody().get(1).getId()).isEqualTo(2L);

        verify(movieImageService).getMovieImages(1L);
    }

    @Test
    void testGetMovieImages_ReturnsEmptyList_WhenNoImages() {
        when(movieImageService.getMovieImages(1L)).thenReturn(List.of());

        ResponseEntity<List<MovieImageResponse>> result = movieImageController.getMovieImages(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).isEmpty();
    }

    @Test
    void getMovieImages_PropagatesMovieNotFoundException() {
        when(movieImageService.getMovieImages(99L))
                .thenThrow(new MovieNotFoundException(99L));

        assertThatThrownBy(() -> movieImageController.getMovieImages(99L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");
    }
}