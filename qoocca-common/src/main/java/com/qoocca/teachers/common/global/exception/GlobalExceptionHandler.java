package com.qoocca.teachers.common.global.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        return buildErrorResponse(e.getErrorCode(), e.getErrorCode().getMessage());
    }

    @ExceptionHandler(PropertyReferenceException.class)
    protected ResponseEntity<ErrorResponse> handlePropertyReferenceException(PropertyReferenceException e) {
        return buildErrorResponse(ErrorCode.INVALID_INPUT_VALUE, ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    protected ResponseEntity<ErrorResponse> handleBadRequestExceptions(Exception e) {
        logger.warn("Invalid request: {}", e.getMessage());
        return buildErrorResponse(ErrorCode.INVALID_INPUT_VALUE, ErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
        logger.error("Unhandled exception", e);
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(ErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.builder()
                        .status(errorCode.getStatus())
                        .code(errorCode.getCode())
                        .message(message)
                        .build());
    }
}
