package com.thewizecompany.billnova.exception;

import com.thewizecompany.billnova.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid credentials", "INVALID_CREDENTIALS", null);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleLocked(LockedException ex) {
        return build(HttpStatus.LOCKED, ex.getMessage(), "ACCOUNT_LOCKED", null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabled(DisabledException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), "ACCOUNT_DISABLED", null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication failed", "AUTHENTICATION_FAILED", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action",
                "ACCESS_DENIED", null);
    }

    /** Catch-all: full trace goes to the log only, client gets a generic message + reference id. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        String correlationId = UUID.randomUUID().toString();
        log.error("Unhandled exception [correlationId={}]", correlationId, ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Quote the reference id when contacting support.",
                "INTERNAL_ERROR", correlationId);
    }

    /** Standard Spring MVC errors (400/404/405/415, validation, bad JSON) all pass through here. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @NonNull Exception ex, Object body, @NonNull HttpHeaders headers, @NonNull HttpStatusCode statusCode, @NonNull WebRequest request) {

        String message;
        String code;

        if (ex instanceof MethodArgumentNotValidException manve) {
            message = manve.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            code = "VALIDATION_FAILED";
        } else if (statusCode.value() == HttpStatus.BAD_REQUEST.value()) {
            message = "Malformed or invalid request";
            code = "BAD_REQUEST";
        } else if (statusCode.value() == HttpStatus.NOT_FOUND.value()) {
            message = "Resource not found";
            code = "NOT_FOUND";
        } else if (statusCode.is5xxServerError()) {
            String correlationId = UUID.randomUUID().toString();
            log.error("Server error [correlationId={}]", correlationId, ex);
            return ResponseEntity.status(statusCode).headers(headers).body(
                    ApiResponse.<Void>error("An unexpected error occurred", "INTERNAL_ERROR", correlationId));
        } else {
            message = "Request could not be processed";
            code = "REQUEST_REJECTED";
        }

        return ResponseEntity.status(statusCode).headers(headers)
                .body(ApiResponse.<Void>error(message, code));
    }

    private ResponseEntity<ApiResponse<Void>> build(
            HttpStatus status, String message, String code, String correlationId) {
        ApiResponse<Void> body = (correlationId == null)
                ? ApiResponse.error(message, code)
                : ApiResponse.error(message, code, correlationId);
        return ResponseEntity.status(status).body(body);
    }
}