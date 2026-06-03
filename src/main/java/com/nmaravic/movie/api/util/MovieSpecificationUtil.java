package com.nmaravic.movie.api.util;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.model.Genre;
import org.springframework.data.jpa.domain.Specification;

public final class MovieSpecificationUtil {

    private static final String TITLE = "title";
    private static final String OVERVIEW = "overview";
    private static final String GENRES = "genres";
    private static final String RELEASE_YEAR = "releaseYear";
    private static final String AVG_RATING = "avgRating";

    private MovieSpecificationUtil() {}

    public static Specification<Movie> hasQuery(String query) {
        return (root, cq, cb) -> query == null ? null :
                cb.or(cb.like(cb.lower(root.get(TITLE)), "%" + query.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get(OVERVIEW)), "%" + query.toLowerCase() + "%"));
    }

    public static Specification<Movie> hasGenre(Genre genre) {
        return (root, cq, cb) -> genre == null ? null :
                cb.isMember(genre, root.get(GENRES));
    }

    public static Specification<Movie> hasYear(Integer year) {
        return (root, cq, cb) -> year == null ? null :
                cb.equal(root.get(RELEASE_YEAR), year);
    }

    public static Specification<Movie> hasMinRating(Double minRating) {
        return (root, cq, cb) -> minRating == null ? null :
                cb.greaterThanOrEqualTo(root.get(AVG_RATING), minRating);
    }
}
