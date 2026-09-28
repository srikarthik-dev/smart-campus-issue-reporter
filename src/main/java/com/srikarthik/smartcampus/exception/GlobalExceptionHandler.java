package com.srikarthik.smartcampus.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler — converts all application exceptions into
 * structured JSON error responses with a consistent shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IssueNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(IssueNotFoundException ex,
                                                        HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Issue Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidWorkflowTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidWorkflowTransitionException ex,
                                                                  HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Invalid Workflow Transition", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidAssignmentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAssignment(InvalidAssignmentException ex,
                                                                  HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Assignment", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "One or more fields failed validation",
                request.getRequestURI()
        );
        response.setFieldErrors(fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        ex.printStackTrace();
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again.", request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error,
                                                         String message, String path) {
        ErrorResponse response = new ErrorResponse(LocalDateTime.now(), status.value(), error, message, path);
        return ResponseEntity.status(status).body(response);
    }

    // --- Inner error response class ---

    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private Map<String, String> fieldErrors;

        public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }

        public LocalDateTime getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public Map<String, String> getFieldErrors() { return fieldErrors; }
        public void setFieldErrors(Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; }
    }
}
