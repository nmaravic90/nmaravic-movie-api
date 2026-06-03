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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class MovieImageService {

    private static final int MAX_SLIDE_IMAGES = 3;

    @Value("${movie.image.upload.dir}")
    private String uploadDir;

    @Value("${movie.image.server.base.url}")
    private String serverBaseUrl;

    private final MovieImageRepository movieImageRepository;
    private final MovieRepository movieRepository;
    private final MovieImageMapper movieImageMapper;

    public MovieImageService(MovieImageRepository movieImageRepository,
                             MovieRepository movieRepository,
                             MovieImageMapper movieImageMapper) {
        this.movieImageRepository = movieImageRepository;
        this.movieRepository = movieRepository;
        this.movieImageMapper = movieImageMapper;
    }

    @Transactional
    public MovieImageResponse uploadMovieImage(Long movieId, MultipartFile file, ImageType type) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        int slideCount = getSlideCount(movieId, type);
        validateImageLimit(movieId, type, slideCount);
        String url = saveImageAndGetUrl(movieId, file, type, slideCount);

        MovieImage image = movieImageMapper.mapMovieImage(movie, url, type);
        MovieImage saved = movieImageRepository.save(image);
        return movieImageMapper.mapMovieImageResponse(saved);
    }

    public List<MovieImageResponse> getMovieImages(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new MovieNotFoundException(movieId);
        }
        return movieImageRepository.findAllByMovie_MovieId(movieId)
                .stream()
                .map(movieImageMapper::mapMovieImageResponse)
                .toList();
    }

    @Transactional
    public void deleteAllMovieImages(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new MovieNotFoundException(movieId);
        }

        List<MovieImage> images = movieImageRepository.findAllByMovie_MovieId(movieId);
        images.forEach(image -> FileStorageUtil.deleteFile(uploadDir, image.getUrl(), serverBaseUrl));
        FileStorageUtil.deleteMovieFolder(uploadDir, movieId);

        movieImageRepository.deleteAllByMovie_MovieId(movieId);
    }


    private void validateImageLimit(Long movieId, ImageType type, int slideCount) {
        if (type == ImageType.COVER) {
            if (movieImageRepository.existsByMovie_MovieIdAndType(movieId, ImageType.COVER)) {
                throw new MovieImageLimitException("Cover image already exists for movie " + movieId);
            }
        }
        else {
            if (slideCount >= MAX_SLIDE_IMAGES) {
                throw new MovieImageLimitException("Maximum slide limit (3) reached for movie " + movieId);
            }
        }
    }

    private String saveImageAndGetUrl(Long movieId, MultipartFile file, ImageType type, int slideCount) {
        String filename = resolveFilename(type, slideCount);
        FileStorageUtil.saveFile(uploadDir, movieId, file, filename);
        return buildImageUrl(movieId, filename);
    }
    
    private int getSlideCount(Long movieId, ImageType type) {
        int slideCount = 0;
        if (type == ImageType.SLIDE) {
            slideCount = movieImageRepository.countByMovie_MovieIdAndType(movieId, ImageType.SLIDE);
        }
        return slideCount;
    }

    private String resolveFilename(ImageType type, int slideCount) {
        return type == ImageType.COVER ? "cover.jpg" : "slide" + (slideCount + 1) + ".jpg";
    }

    private String buildImageUrl(Long movieId, String filename) {
        return serverBaseUrl + "/" + movieId + "/" + filename;
    }
}