package com.nmaravic.movie.api.util;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import com.nmaravic.movie.api.model.Genre;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieSpecificationUtilTest {

    @Mock
    private Root<Movie> root;

    @Mock
    private CriteriaQuery<?> criteriaQuery;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<String> titlePath;

    @Mock
    private Path<String> overviewPath;

    @Mock
    private Path<Integer> yearPath;

    @Mock
    private Path<Double> ratingPath;

    @Mock
    private Expression<Genre> genresExpression;

    @Mock
    private Expression<String> lowerTitleExpr;

    @Mock
    private Expression<String> lowerOverviewExpr;

    @Mock
    private Predicate predicate;

    @Test
    void testHasQuery_ReturnsNull_WhenQueryIsNull() {
        Specification<Movie> spec = MovieSpecificationUtil.hasQuery(null);
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertThat(result).isNull();
        verifyNoInteractions(root, criteriaBuilder);
    }

    @Test
    void testHasQuery_ReturnsPredicate_WhenQueryIsProvided() {
        when(root.<String>get("title")).thenReturn(titlePath);
        when(root.<String>get("overview")).thenReturn(overviewPath);
        when(criteriaBuilder.lower(titlePath)).thenReturn(lowerTitleExpr);
        when(criteriaBuilder.lower(overviewPath)).thenReturn(lowerOverviewExpr);
        when(criteriaBuilder.like(eq(lowerTitleExpr), anyString())).thenReturn(predicate);
        when(criteriaBuilder.like(eq(lowerOverviewExpr), anyString())).thenReturn(predicate);
        when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

        Specification<Movie> spec = MovieSpecificationUtil.hasQuery("inception");
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(criteriaBuilder).like(lowerTitleExpr, "%inception%");
        verify(criteriaBuilder).like(lowerOverviewExpr, "%inception%");
        verify(criteriaBuilder).or(any(Predicate.class), any(Predicate.class));
    }

    @Test
    void testHasQuery_UsesLowercase_WhenQueryHasUppercase() {
        when(root.<String>get("title")).thenReturn(titlePath);
        when(root.<String>get("overview")).thenReturn(overviewPath);
        when(criteriaBuilder.lower(titlePath)).thenReturn(lowerTitleExpr);
        when(criteriaBuilder.lower(overviewPath)).thenReturn(lowerOverviewExpr);
        when(criteriaBuilder.like(eq(lowerTitleExpr), anyString())).thenReturn(predicate);
        when(criteriaBuilder.like(eq(lowerOverviewExpr), anyString())).thenReturn(predicate);
        when(criteriaBuilder.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

        Specification<Movie> spec = MovieSpecificationUtil.hasQuery("INCEPTION");

        spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder).like(lowerTitleExpr, "%inception%");
        verify(criteriaBuilder).like(lowerOverviewExpr, "%inception%");
    }

    @Test
    void testHasGenre_ReturnsNull_WhenGenreIsNull() {
        Specification<Movie> spec = MovieSpecificationUtil.hasGenre(null);

        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertThat(result).isNull();
        verifyNoInteractions(root, criteriaBuilder);
    }

    @Test
    void testHasYear_ReturnsNull_WhenYearIsNull() {
        Specification<Movie> spec = MovieSpecificationUtil.hasYear(null);

        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertThat(result).isNull();
        verifyNoInteractions(root, criteriaBuilder);
    }

    @Test
    void testHasYear_ReturnsPredicate_WhenYearIsProvided() {
        when(root.<Integer>get("releaseYear")).thenReturn(yearPath);
        when(criteriaBuilder.equal(yearPath, 2010)).thenReturn(predicate);

        Specification<Movie> spec = MovieSpecificationUtil.hasYear(2010);

        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(root).get("releaseYear");
        verify(criteriaBuilder).equal(yearPath, 2010);
    }

    @Test
    void testHasMinRating_ReturnsNull_WhenMinRatingIsNull() {
        Specification<Movie> spec = MovieSpecificationUtil.hasMinRating(null);

        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertThat(result).isNull();
        verifyNoInteractions(root, criteriaBuilder);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHasMinRating_ReturnsPredicate_WhenMinRatingIsProvided() {
        when(root.<Double>get("avgRating")).thenReturn((Path) ratingPath);
        when(criteriaBuilder.greaterThanOrEqualTo(any(Expression.class), eq(8.0))).thenReturn(predicate);

        Specification<Movie> spec = MovieSpecificationUtil.hasMinRating(8.0);

        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertThat(result).isNotNull();
        verify(root).get("avgRating");
        verify(criteriaBuilder).greaterThanOrEqualTo(any(Expression.class), eq(8.0));
    }
}