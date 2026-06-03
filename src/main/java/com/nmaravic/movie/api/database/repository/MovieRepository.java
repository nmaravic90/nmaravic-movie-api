package com.nmaravic.movie.api.database.repository;

import com.nmaravic.movie.api.database.entitymodel.Movie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {

    boolean existsByTitleAndReleaseYear(String title, Integer releaseYear);

    List<Movie> findAllByOrderByAvgRatingDesc(Pageable pageable);
}
