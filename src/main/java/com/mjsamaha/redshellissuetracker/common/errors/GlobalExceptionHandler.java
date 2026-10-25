package com.mjsamaha.redshellissuetracker.common.errors;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail conflict(ConflictException exception) {
        return problem(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(QuotaExceededException.class)
    ProblemDetail quotaExceeded(QuotaExceededException exception) {
        return problem(HttpStatus.TOO_MANY_REQUESTS, exception);
    }

    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail badRequest(InvalidRequestException exception) {
        return problem(HttpStatus.BAD_REQUEST, exception);
    }

    private ProblemDetail problem(HttpStatus status, RuntimeException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        detail.setType(URI.create("about:blank"));
        detail.setTitle(status.getReasonPhrase());
        return detail;
    }
}
