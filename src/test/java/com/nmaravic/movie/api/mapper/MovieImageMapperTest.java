package com.nmaravic.movie.api.mapper;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.database.entitymodel.MovieImage;
import com.nmaravic.movie.api.model.ImageType;
import com.nmaravic.movie.api.model.MovieImageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MovieImageMapperTest {

    private MovieImageMapper mapper;
    private Movie testMovie;

    @BeforeEach
    void setUp() {
        mapper = new MovieImageMapper();
        testMovie = createTestMovie();
    }

    @Test
    void testMapMovieImage_WithMovieUrlAndType() {
        String url = "http://localhost:8080/images/1/cover.jpg";
        ImageType type = ImageType.COVER;

        MovieImage result = mapper.mapMovieImage(testMovie, url, type);

        assertThat(result).isNotNull();
        assertThat(result.getMovie()).isEqualTo(testMovie);
        assertThat(result.getUrl()).isEqualTo(url);
        assertThat(result.getType()).isEqualTo(type);
    }

    @Test
    void testMapMovieImage_WithSlideType() {
        String url = "http://localhost:8080/images/1/slide1.jpg";
        ImageType type = ImageType.SLIDE;

        MovieImage result = mapper.mapMovieImage(testMovie, url, type);

        assertThat(result.getType()).isEqualTo(ImageType.SLIDE);
        assertThat(result.getUrl()).isEqualTo(url);
        assertThat(result.getMovie()).isEqualTo(testMovie);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "http://localhost:8080/images/1/cover.jpg",
        "http://localhost:8080/images/1/slide1.jpg",
        "http://localhost:8080/images/1/slide2.jpg",
        "/images/1/cover.jpg",
        "C:/uploads/1/cover.jpg"
    })
    void testMapMovieImage_WithDifferentUrls(String url) {
        MovieImage result = mapper.mapMovieImage(testMovie, url, ImageType.COVER);
        assertThat(result.getUrl()).isEqualTo(url);
    }

    @Test
    void testMapMovieImage_WithNullUrl() {
        MovieImage result = mapper.mapMovieImage(testMovie, null, ImageType.COVER);

        assertThat(result).isNotNull();
        assertThat(result.getUrl()).isNull();
        assertThat(result.getMovie()).isEqualTo(testMovie);
        assertThat(result.getType()).isEqualTo(ImageType.COVER);
    }

    @Test
    void testMapMovieImage_WithEmptyUrl() {
        String emptyUrl = "";

        MovieImage result = mapper.mapMovieImage(testMovie, emptyUrl, ImageType.COVER);

        assertThat(result.getUrl()).isEqualTo(emptyUrl);
        assertThat(result.getMovie()).isEqualTo(testMovie);
    }

    @Test
    void testMapMovieImage_ImageIdNotSet() {
        MovieImage result = mapper.mapMovieImage(testMovie, "http://test.jpg", ImageType.COVER);

        assertThat(result.getImageId()).isNull();
    }

    @Test
    void testMapMovieImage_CreatedAtNotSet() {
        MovieImage result = mapper.mapMovieImage(testMovie, "http://test.jpg", ImageType.COVER);

        assertThat(result.getCreatedAt()).isNull();
    }

    @Test
    void testMapMovieImage_FromResponse() {
        Long imageId = 1L;
        String url = "http://localhost:8080/images/1/cover.jpg";
        ImageType type = ImageType.COVER;
        LocalDate createdAt = LocalDate.now();

        MovieImageResponse response = new MovieImageResponse()
            .id(imageId)
            .url(url)
            .type(type)
            .createdAt(createdAt);

        MovieImage result = mapper.mapMovieImage(response);

        assertThat(result).isNotNull();
        assertThat(result.getImageId()).isEqualTo(imageId);
        assertThat(result.getUrl()).isEqualTo(url);
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getMovie()).isNull();
    }

    @Test
    void testMapMovieImage_FromResponse_WithSlideType() {
        MovieImageResponse response = new MovieImageResponse()
            .id(2L)
            .url("http://localhost:8080/images/1/slide1.jpg")
            .type(ImageType.SLIDE)
            .createdAt(LocalDate.now());

        MovieImage result = mapper.mapMovieImage(response);

        assertThat(result.getType()).isEqualTo(ImageType.SLIDE);
        assertThat(result.getImageId()).isEqualTo(2L);
    }

    @Test
    void testMapMovieImage_FromResponse_WithNullUrl() {
        MovieImageResponse response = new MovieImageResponse()
            .id(1L)
            .url(null)
            .type(ImageType.COVER)
            .createdAt(LocalDate.now());

        MovieImage result = mapper.mapMovieImage(response);

        assertThat(result.getUrl()).isNull();
        assertThat(result.getImageId()).isEqualTo(1L);
    }

    @Test
    void testMapMovieImage_FromResponse_MovieNotSet() {
        MovieImageResponse response = new MovieImageResponse()
            .id(1L)
            .url("http://test.jpg")
            .type(ImageType.COVER)
            .createdAt(LocalDate.now());

        MovieImage result = mapper.mapMovieImage(response);

        assertThat(result.getMovie()).isNull();
    }


    @Test
    void testMapMovieImageResponse_WithValidMovieImage() {
        MovieImage movieImage = createTestMovieImage();

        MovieImageResponse result = mapper.mapMovieImageResponse(movieImage);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(movieImage.getImageId());
        assertThat(result.getUrl()).isEqualTo(movieImage.getUrl());
        assertThat(result.getType()).isEqualTo(movieImage.getType());
        assertThat(result.getCreatedAt()).isEqualTo(movieImage.getCreatedAt().toLocalDate());
    }

    @Test
    void testMapMovieImageResponse_DateTimeConversion() {
        LocalDateTime createdAt = LocalDateTime.of(2024, 5, 15, 10, 30, 45);
        MovieImage movieImage = new MovieImage();
        movieImage.setImageId(1L);
        movieImage.setUrl("http://test.jpg");
        movieImage.setType(ImageType.COVER);
        movieImage.setCreatedAt(createdAt);

        MovieImageResponse result = mapper.mapMovieImageResponse(movieImage);

        assertThat(result.getCreatedAt()).isEqualTo(createdAt.toLocalDate());
        assertThat(result.getCreatedAt()).isEqualTo(LocalDate.of(2024, 5, 15));
    }

    @Test
    void testMapMovieImageResponse_WithCoverType() {
        MovieImage movieImage = createTestMovieImage();
        movieImage.setType(ImageType.COVER);

        MovieImageResponse result = mapper.mapMovieImageResponse(movieImage);

        assertThat(result.getType()).isEqualTo(ImageType.COVER);
    }

    @Test
    void testMapMovieImageResponse_WithSlideType() {
        MovieImage movieImage = createTestMovieImage();
        movieImage.setType(ImageType.SLIDE);

        MovieImageResponse result = mapper.mapMovieImageResponse(movieImage);

        assertThat(result.getType()).isEqualTo(ImageType.SLIDE);
    }

    @Test
    void testMapMovieImageResponse_AllFieldsPreserved() {
        Long imageId = 5L;
        String url = "http://localhost:8080/images/99/slide2.jpg";
        ImageType type = ImageType.SLIDE;
        LocalDateTime createdAt = LocalDateTime.now();

        MovieImage movieImage = new MovieImage();
        movieImage.setImageId(imageId);
        movieImage.setUrl(url);
        movieImage.setType(type);
        movieImage.setCreatedAt(createdAt);

        MovieImageResponse result = mapper.mapMovieImageResponse(movieImage);

        assertThat(result.getId()).isEqualTo(imageId);
        assertThat(result.getUrl()).isEqualTo(url);
        assertThat(result.getType()).isEqualTo(type);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt.toLocalDate());
    }

    @Test
    void testMapMovieImageResponse_MovieNotIncluded() {
        MovieImage movieImage = createTestMovieImage();
        movieImage.setMovie(testMovie);

        MovieImageResponse result = mapper.mapMovieImageResponse(movieImage);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(movieImage.getImageId());
    }

    @Test
    void testMapper_MultipleSequentialMappings() {
        MovieImage image1 = mapper.mapMovieImage(testMovie, "url1.jpg", ImageType.COVER);
        MovieImage image2 = mapper.mapMovieImage(testMovie, "url2.jpg", ImageType.SLIDE);
        MovieImage image3 = mapper.mapMovieImage(testMovie, "url3.jpg", ImageType.SLIDE);

        assertThat(image1.getUrl()).isEqualTo("url1.jpg");
        assertThat(image2.getUrl()).isEqualTo("url2.jpg");
        assertThat(image3.getUrl()).isEqualTo("url3.jpg");
        assertThat(image1.getType()).isEqualTo(ImageType.COVER);
        assertThat(image2.getType()).isEqualTo(ImageType.SLIDE);
        assertThat(image3.getType()).isEqualTo(ImageType.SLIDE);
    }

    @Test
    void testMapper_directionalMapping() {
        MovieImage movieImage = createTestMovieImage();
        MovieImageResponse response = mapper.mapMovieImageResponse(movieImage);
        MovieImage movieImageFromResponse = mapper.mapMovieImage(response);

        assertThat(movieImageFromResponse.getImageId()).isEqualTo(response.getId());
        assertThat(movieImageFromResponse.getUrl()).isEqualTo(response.getUrl());
        assertThat(movieImageFromResponse.getType()).isEqualTo(response.getType());
    }

    @Test
    void testMapper_CreatesNewInstances() {
        MovieImage image1 = mapper.mapMovieImage(testMovie, "url.jpg", ImageType.COVER);
        MovieImage image2 = mapper.mapMovieImage(testMovie, "url.jpg", ImageType.COVER);

        assertThat(image1).isNotSameAs(image2);
    }

    private Movie createTestMovie() {
        Movie movie = new Movie();
        movie.setMovieId(1L);
        movie.setTitle("Avatar");
        return movie;
    }

    private MovieImage createTestMovieImage() {
        MovieImage movieImage = new MovieImage();
        movieImage.setImageId(1L);
        movieImage.setMovie(testMovie);
        movieImage.setUrl("http://localhost:8080/images/1/cover.jpg");
        movieImage.setType(ImageType.COVER);
        movieImage.setCreatedAt(LocalDateTime.now());
        return movieImage;
    }
}