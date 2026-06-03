package com.nmaravic.movie.api.database.repository;

import com.nmaravic.movie.api.database.entitymodel.MovieImage;
import com.nmaravic.movie.api.model.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieImageRepository extends JpaRepository<MovieImage, Long> {

    boolean existsByMovie_MovieIdAndType(Long movieId, ImageType imageType);

    int countByMovie_MovieIdAndType(Long movieId, ImageType imageType);

    List<MovieImage> findAllByMovie_MovieId(Long movieId);

    void deleteAllByMovie_MovieId(Long movieId);
}
