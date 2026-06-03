package com.nmaravic.movie.api.mapper;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.model.Genre;
import com.nmaravic.movie.api.model.MovieRequest;
import com.nmaravic.movie.api.model.MovieResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MovieMapperTest {

	private MovieMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new MovieMapper();
	}

	@Test
	void testMapMovie_FromRequest_AllFields() {
		MovieRequest request = new MovieRequest()
				.title("Inception")
				.overview("A mind-bending thriller")
				.releaseYear(2010)
				.director("Christopher Nolan")
				.genres(List.of(Genre.SCI_FI, Genre.THRILLER));

		Movie result = mapper.mapMovie(request);

		assertThat(result).isNotNull();
		assertThat(result.getMovieId()).isNull();
		assertThat(result.getTitle()).isEqualTo("Inception");
		assertThat(result.getOverview()).isEqualTo("A mind-bending thriller");
		assertThat(result.getReleaseYear()).isEqualTo(2010);
		assertThat(result.getDirector()).isEqualTo("Christopher Nolan");
		assertThat(result.getGenres()).containsExactly(Genre.SCI_FI, Genre.THRILLER);
		assertThat(result.getAvgRating()).isNull();
		assertThat(result.getRatingCount()).isNull();
		assertThat(result.getCreatedAt()).isNull();
		assertThat(result.getUpdatedAt()).isNull();
	}

	@Test
	void testMapMovieResponse_WithValidMovie() {
		LocalDateTime createdAt = LocalDateTime.of(2023, 3, 10, 12, 0);
		LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 5, 8, 30);

		Movie movie = new Movie();
		movie.setMovieId(42L);
		movie.setTitle("The Matrix");
		movie.setOverview("Simulation") ;
		movie.setReleaseYear(1999);
		movie.setDirector("Wachowskis");
		movie.setGenres(List.of(Genre.SCI_FI, Genre.ACTION));
		movie.setAvgRating(9.1);
		movie.setRatingCount(1234);
		movie.setCreatedAt(createdAt);
		movie.setUpdatedAt(updatedAt);

		MovieResponse response = mapper.mapMovieResponse(movie);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(42L);
		assertThat(response.getTitle()).isEqualTo("The Matrix");
		assertThat(response.getOverview()).isEqualTo("Simulation");
		assertThat(response.getReleaseYear()).isEqualTo(1999);
		assertThat(response.getDirector()).isEqualTo("Wachowskis");
		assertThat(response.getGenres()).containsExactly(Genre.SCI_FI, Genre.ACTION);
		assertThat(response.getAvgRating()).isEqualTo(9.1);
		assertThat(response.getRatingCount()).isEqualTo(1234);
		assertThat(response.getCreatedAt()).isEqualTo(createdAt.toLocalDate());
		assertThat(response.getUpdatedAt()).isEqualTo(updatedAt.toLocalDate());
		assertThat(response.getCreatedAt()).isEqualTo(LocalDate.of(2023, 3, 10));
		assertThat(response.getUpdatedAt()).isEqualTo(LocalDate.of(2024, 1, 5));
	}

	@Test
	void testMapper_CreatesNewInstances() {
		MovieRequest request = new MovieRequest()
				.title("Dune")
				.releaseYear(2021)
				.director("Denis Villeneuve")
				.genres(List.of(Genre.SCI_FI));

		Movie m1 = mapper.mapMovie(request);
		Movie m2 = mapper.mapMovie(request);

		assertThat(m1).isNotSameAs(m2);
	}
}