package com.example.demo.api;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    ProblemDetail status(org.springframework.web.server.ResponseStatusException exception) {
        var detail=ProblemDetail.forStatusAndDetail(exception.getStatusCode(),exception.getReason()==null?"Request failed":exception.getReason());
        detail.setProperty("code",exception.getReason());
        return detail;
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail invalidUrl(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ProblemDetail invalidBody(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "JSON không hợp lệ hoặc thiếu trường bắt buộc.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail conflict(DataIntegrityViolationException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Repository đã được đăng ký hoặc dữ liệu xung đột.");
    }
}
