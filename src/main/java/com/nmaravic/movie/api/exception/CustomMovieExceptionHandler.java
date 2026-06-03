package com.nmaravic.movie.api.exception;


import com.nmaravic.movie.api.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;

@RestControllerAdvice
public class CustomMovieExceptionHandler {

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMovieNotFound(MovieNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(HtmlUtils.htmlEscape(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MovieAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleMovieAlreadyExists(MovieAlreadyExistsException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse()
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(HtmlUtils.htmlEscape(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MovieReviewAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleReviewAlreadyExists(MovieReviewAlreadyExistsException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse()
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(HtmlUtils.htmlEscape(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MovieReviewNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReviewNotFound(MovieReviewNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(HtmlUtils.htmlEscape(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MovieImageLimitException.class)
    public ResponseEntity<ErrorResponse> handleReviewAlreadyExists(MovieImageLimitException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse()
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(HtmlUtils.htmlEscape(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
