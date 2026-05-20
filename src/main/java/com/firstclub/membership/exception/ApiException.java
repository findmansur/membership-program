package com.firstclub.membership.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for domain-level errors that should be translated into well-formed
 * HTTP responses by {@link GlobalExceptionHandler}. Using a small hierarchy
 * (rather than scattering {@code ResponseStatusException}s through the code)
 * keeps services free of HTTP concerns.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
