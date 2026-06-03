package com.nmaravic.movie.api.database.repository;

import com.nmaravic.movie.api.database.entitymodel.MovieReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieReviewRepository extends JpaRepository<MovieReview, Long> {

    boolean existsByMovie_MovieIdAndUsername(Long movieId, String username);

    Page<MovieReview> findAllByMovie_MovieId(Long movieId, Pageable pageable);

    void deleteByReviewIdAndMovie_MovieId(Long reviewId, Long movieId);
}
