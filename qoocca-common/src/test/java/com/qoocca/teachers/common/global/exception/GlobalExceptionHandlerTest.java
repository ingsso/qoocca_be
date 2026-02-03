package com.qoocca.teachers.common.global.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unexpectedExceptionReturnsStandardInternalErrorResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpectedException(new RuntimeException("sensitive"));

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), response.getBody().code());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), response.getBody().message());
    }

    @Test
    void validationExceptionReturnsInvalidInputResponse() {
        MethodArgumentNotValidException validationException = mock(MethodArgumentNotValidException.class);

        ResponseEntity<ErrorResponse> response = handler.handleBadRequestExceptions(validationException);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INVALID_INPUT_VALUE.getCode(), response.getBody().code());
        assertEquals(ErrorCode.INVALID_INPUT_VALUE.getMessage(), response.getBody().message());
    }
}
