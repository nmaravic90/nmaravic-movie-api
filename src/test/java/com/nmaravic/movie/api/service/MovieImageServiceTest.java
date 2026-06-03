package com.nmaravic.movie.api.service;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.database.entitymodel.MovieImage;
import com.nmaravic.movie.api.database.repository.MovieImageRepository;
import com.nmaravic.movie.api.database.repository.MovieRepository;
import com.nmaravic.movie.api.exception.MovieImageLimitException;
import com.nmaravic.movie.api.exception.MovieNotFoundException;
import com.nmaravic.movie.api.mapper.MovieImageMapper;
import com.nmaravic.movie.api.model.ImageType;
import com.nmaravic.movie.api.model.MovieImageResponse;
import com.nmaravic.movie.api.util.FileStorageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieImageServiceTest {

    @Mock
    private MovieImageRepository movieImageRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieImageMapper movieImageMapper;

    @InjectMocks
    private MovieImageService movieImageService;

    private Movie movie;
    private MovieImage movieImage;
    private MovieImageResponse imageResponse;
    private MultipartFile mockFile;

    private static final String UPLOAD_DIR = "/uploads";
    private static final String SERVER_BASE_URL = "http://localhost:8080/images";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(movieImageService, "uploadDir", UPLOAD_DIR);
        ReflectionTestUtils.setField(movieImageService, "serverBaseUrl", SERVER_BASE_URL);

        movie = new Movie();
        movie.setMovieId(1L);
        movie.setTitle("Inception");

        movieImage = new MovieImage();
        movieImage.setMovie(movie);
        movieImage.setUrl(SERVER_BASE_URL + "/1/cover.jpg");
        movieImage.setType(ImageType.COVER);

        imageResponse = new MovieImageResponse();
        imageResponse.setId(1L);
        imageResponse.setUrl(SERVER_BASE_URL + "/1/cover.jpg");
        imageResponse.setType(ImageType.COVER);

        mockFile = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "fake-image".getBytes()
        );
    }

    @Test
    void testUploadMovieImage_Cover_Success() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieImageRepository.existsByMovie_MovieIdAndType(1L, ImageType.COVER)).thenReturn(false);
        when(movieImageMapper.mapMovieImage(eq(movie), any(String.class), eq(ImageType.COVER))).thenReturn(movieImage);
        when(movieImageRepository.save(movieImage)).thenReturn(movieImage);
        when(movieImageMapper.mapMovieImageResponse(movieImage)).thenReturn(imageResponse);

        try (MockedStatic<FileStorageUtil> fileStorageUtil = mockStatic(FileStorageUtil.class)) {
            fileStorageUtil.when(() -> FileStorageUtil.saveFile(eq(UPLOAD_DIR), eq(1L), eq(mockFile), eq("cover.jpg")))
                    .thenAnswer(invocation -> null);

            MovieImageResponse result = movieImageService.uploadMovieImage(1L, mockFile, ImageType.COVER);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getType()).isEqualTo(ImageType.COVER);
            assertThat(result.getUrl()).isEqualTo(SERVER_BASE_URL + "/1/cover.jpg");

            fileStorageUtil.verify(() -> FileStorageUtil.saveFile(eq(UPLOAD_DIR), eq(1L), eq(mockFile), eq("cover.jpg")));
        }

        verify(movieRepository).findById(1L);
        verify(movieImageRepository).existsByMovie_MovieIdAndType(1L, ImageType.COVER);
        verify(movieImageRepository).save(movieImage);
    }

    @Test
    void testUploadMovieImage_Slide_Success() {
        MovieImage slideImage = new MovieImage();
        slideImage.setMovie(movie);
        slideImage.setUrl(SERVER_BASE_URL + "/1/slide1.jpg");
        slideImage.setType(ImageType.SLIDE);

        MovieImageResponse slideResponse = new MovieImageResponse();
        slideResponse.setId(2L);
        slideResponse.setUrl(SERVER_BASE_URL + "/1/slide1.jpg");
        slideResponse.setType(ImageType.SLIDE);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieImageRepository.countByMovie_MovieIdAndType(1L, ImageType.SLIDE)).thenReturn(0);
        when(movieImageMapper.mapMovieImage(eq(movie), any(String.class), eq(ImageType.SLIDE))).thenReturn(slideImage);
        when(movieImageRepository.save(slideImage)).thenReturn(slideImage);
        when(movieImageMapper.mapMovieImageResponse(slideImage)).thenReturn(slideResponse);

        try (MockedStatic<FileStorageUtil> fileStorageUtil = mockStatic(FileStorageUtil.class)) {
            fileStorageUtil.when(() -> FileStorageUtil.saveFile(eq(UPLOAD_DIR), eq(1L), any(MultipartFile.class), eq("slide1.jpg")))
                    .thenAnswer(invocation -> null);

            MovieImageResponse result = movieImageService.uploadMovieImage(1L, mockFile, ImageType.SLIDE);

            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(ImageType.SLIDE);
            assertThat(result.getUrl()).isEqualTo(SERVER_BASE_URL + "/1/slide1.jpg");
        }
    }

    @Test
    void testUploadMovieImage_ThrowsMovieNotFoundException_WhenMovieNotFound() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieImageService.uploadMovieImage(99L, mockFile, ImageType.COVER))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");

        verify(movieImageRepository, never()).save(any());
    }

    @Test
    void testUploadMovieImage_ThrowsMovieImageLimitException_WhenCoverAlreadyExists() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieImageRepository.existsByMovie_MovieIdAndType(1L, ImageType.COVER)).thenReturn(true);

        assertThatThrownBy(() -> movieImageService.uploadMovieImage(1L, mockFile, ImageType.COVER))
                .isInstanceOf(MovieImageLimitException.class)
                .hasMessageContaining("Cover image already exists");

        verify(movieImageRepository, never()).save(any());
    }

    @Test
    void testUploadMovieImage_ThrowsMovieImageLimitException_WhenSlideMaxReached() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieImageRepository.countByMovie_MovieIdAndType(1L, ImageType.SLIDE)).thenReturn(3);

        assertThatThrownBy(() -> movieImageService.uploadMovieImage(1L, mockFile, ImageType.SLIDE))
                .isInstanceOf(MovieImageLimitException.class)
                .hasMessageContaining("Maximum slide limit");

        verify(movieImageRepository, never()).save(any());
    }

    @Test
    void testGetMovieImages_Success() {
        when(movieRepository.existsById(1L)).thenReturn(true);
        when(movieImageRepository.findAllByMovie_MovieId(1L)).thenReturn(List.of(movieImage));
        when(movieImageMapper.mapMovieImageResponse(movieImage)).thenReturn(imageResponse);

        List<MovieImageResponse> result = movieImageService.getMovieImages(1L);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getType()).isEqualTo(ImageType.COVER);

        verify(movieRepository).existsById(1L);
        verify(movieImageRepository).findAllByMovie_MovieId(1L);
    }

    @Test
    void testGetMovieImages_ReturnsEmptyList_WhenNoImages() {
        when(movieRepository.existsById(1L)).thenReturn(true);
        when(movieImageRepository.findAllByMovie_MovieId(1L)).thenReturn(List.of());

        List<MovieImageResponse> result = movieImageService.getMovieImages(1L);

        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void testGetMovieImages_ThrowsMovieNotFoundException_WhenMovieNotFound() {
        when(movieRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> movieImageService.getMovieImages(99L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");

        verify(movieImageRepository, never()).findAllByMovie_MovieId(any());
    }

    @Test
    void testDeleteAllMovieImages_Success() {
        when(movieRepository.existsById(1L)).thenReturn(true);
        when(movieImageRepository.findAllByMovie_MovieId(1L)).thenReturn(List.of(movieImage));

        try (MockedStatic<FileStorageUtil> fileStorageUtil = mockStatic(FileStorageUtil.class)) {
            fileStorageUtil.when(() -> FileStorageUtil.deleteFile(any(), any(), any()))
                    .thenAnswer(invocation -> null);
            fileStorageUtil.when(() -> FileStorageUtil.deleteMovieFolder(any(), any()))
                    .thenAnswer(invocation -> null);

            movieImageService.deleteAllMovieImages(1L);

            fileStorageUtil.verify(() -> FileStorageUtil.deleteFile(
                    eq(UPLOAD_DIR), eq(SERVER_BASE_URL + "/1/cover.jpg"), eq(SERVER_BASE_URL)));
            fileStorageUtil.verify(() -> FileStorageUtil.deleteMovieFolder(eq(UPLOAD_DIR), eq(1L)));
        }

        verify(movieRepository).existsById(1L);
        verify(movieImageRepository).findAllByMovie_MovieId(1L);
        verify(movieImageRepository).deleteAllByMovie_MovieId(1L);
    }

    @Test
    void testDeleteAllMovieImages_DeletesAllImages_WhenMultipleExist() {
        MovieImage slideImage = new MovieImage();
        slideImage.setMovie(movie);
        slideImage.setUrl(SERVER_BASE_URL + "/1/slide1.jpg");
        slideImage.setType(ImageType.SLIDE);

        when(movieRepository.existsById(1L)).thenReturn(true);
        when(movieImageRepository.findAllByMovie_MovieId(1L)).thenReturn(List.of(movieImage, slideImage));

        try (MockedStatic<FileStorageUtil> fileStorageUtil = mockStatic(FileStorageUtil.class)) {
            fileStorageUtil.when(() -> FileStorageUtil.deleteFile(any(), any(), any()))
                    .thenAnswer(invocation -> null);
            fileStorageUtil.when(() -> FileStorageUtil.deleteMovieFolder(any(), any()))
                    .thenAnswer(invocation -> null);

            movieImageService.deleteAllMovieImages(1L);

            fileStorageUtil.verify(() -> FileStorageUtil.deleteFile(any(), any(), any()), times(2));
            fileStorageUtil.verify(() -> FileStorageUtil.deleteMovieFolder(eq(UPLOAD_DIR), eq(1L)));
        }

        verify(movieImageRepository).deleteAllByMovie_MovieId(1L);
    }

    @Test
    void testDeleteAllMovieImages_ThrowsMovieNotFoundException_WhenMovieNotFound() {
        when(movieRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> movieImageService.deleteAllMovieImages(99L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessageContaining("99");

        verify(movieImageRepository, never()).deleteAllByMovie_MovieId(any());
    }
}