package com.nmaravic.movie.api.mapper;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.database.entitymodel.MovieReview;
import com.nmaravic.movie.api.model.ReviewPageResponse;
import com.nmaravic.movie.api.model.ReviewRequest;
import com.nmaravic.movie.api.model.ReviewResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MovieReviewMapperTest {

	private MovieReviewMapper mapper;
	private Movie testMovie;

	@BeforeEach
	void setUp() {
		mapper = new MovieReviewMapper();
		testMovie = createTestMovie();
	}

	@Test
	void testMapReview_WithValidRequest() {
		ReviewRequest request = new ReviewRequest()
				.rating(8)
				.comment("Great movie");

		MovieReview result = mapper.mapReview(testMovie, "john.doe", request);

		assertThat(result).isNotNull();
		assertThat(result.getMovie()).isEqualTo(testMovie);
		assertThat(result.getUsername()).isEqualTo("john.doe");
		assertThat(result.getRating()).isEqualTo(8);
		assertThat(result.getComment()).isEqualTo("Great movie");
		assertThat(result.getReviewId()).isNull();
		assertThat(result.getCreatedAt()).isNull();
	}

	@Test
	void testMapReview_WithNullComment() {
		ReviewRequest request = new ReviewRequest()
				.rating(5)
				.comment(null);

		MovieReview result = mapper.mapReview(testMovie, "alice", request);

		assertThat(result.getComment()).isNull();
		assertThat(result.getRating()).isEqualTo(5);
	}

	@Test
	void testMapReviewResponse_WithValidReview() {
		LocalDateTime createdAt = LocalDateTime.of(2024, 5, 15, 10, 30, 45);
		MovieReview review = createTestMovieReview();
		review.setReviewId(10L);
		review.setCreatedAt(createdAt);

		ReviewResponse response = mapper.mapReviewResponse(review);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(10L);
		assertThat(response.getMovieId()).isEqualTo(testMovie.getMovieId());
		assertThat(response.getUsername()).isEqualTo(review.getUsername());
		assertThat(response.getRating()).isEqualTo(review.getRating());
		assertThat(response.getComment()).isEqualTo(review.getComment());
		assertThat(response.getCreatedAt()).isEqualTo(createdAt.toLocalDate());
		assertThat(response.getCreatedAt()).isEqualTo(LocalDate.of(2024, 5, 15));
	}

	@Test
	void testMapReviewPageResponse_WithPage() {
		MovieReview r1 = createTestMovieReview();
		r1.setReviewId(1L);
		MovieReview r2 = createTestMovieReview();
		r2.setReviewId(2L);

		PageImpl<MovieReview> page = new PageImpl<>(List.of(r1, r2), PageRequest.of(0, 2), 2);

		ReviewPageResponse result = mapper.mapReviewPageResponse(page);

		assertThat(result).isNotNull();
		assertThat(result.getPage()).isZero();
		assertThat(result.getSize()).isEqualTo(2);
		assertThat(result.getTotalElements()).isEqualTo(2L);
		assertThat(result.getTotalPages()).isEqualTo(1);

		ReviewResponse first = result.getContent().get(0);
		assertThat(first.getId()).isEqualTo(1L);
	}

	private Movie createTestMovie() {
		Movie movie = new Movie();
		movie.setMovieId(1L);
		movie.setTitle("Avatar");
		return movie;
	}

	private MovieReview createTestMovieReview() {
		MovieReview review = new MovieReview();
		review.setReviewId(null);
		review.setMovie(testMovie);
		review.setUsername("tester");
		review.setRating(9);
		review.setComment("Excellent");
		review.setCreatedAt(LocalDateTime.now());
		return review;
	}
}