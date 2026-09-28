package com.srikarthik.smartcampus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidWorkflowTransitionException extends RuntimeException {
    public InvalidWorkflowTransitionException(String message) {
        super(message);
    }
}
