package com.example.qoocca_be.attendance.exception;/*
package com.example.loginbe.attendance.exception;

import com.example.BackendServer.vehicle.controller.VehicleApiController;
import com.example.BackendServer.vehicle.model.ErrorMsg;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(basePackageClasses = VehicleApiController.class)
public class VehicleApiExceptionHandler {

    */
/**
     * DTO의 enum값이 올바르지 않을 경우 발생하는 예외 처리
     * HttpMessageNotReadableException 발생할 경우 호출
     *
     * @param ex : HttpMessageNotReadableException
     * @return Error 메시지를 반환
     *//*

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMsg> handleEnumMismatchError(HttpMessageNotReadableException ex) {
        //log.error("Json parse error", ex);

        String msg = "잘못된 요청 형식입니다";

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalid) {

            JsonMappingException.Reference ref = invalid.getPath().get(0);
            String field        = ref.getFieldName();
            String rejected     = String.valueOf(invalid.getValue());

            String allowed = Arrays.stream(invalid.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            msg = String.format("%s : { %s } 는 허용되지 않는 값입니다. 허용 가능 값: [%s]",
                    field, rejected, allowed);
        }

        ErrorMsg body = ErrorMsg.builder()
                .errorMessage(List.of(msg))
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    */
/**
     * 클라이언트의 요청에서 오는
     * @Valid 에서 발생한 예외 처리
     *
     * @param ex : MethodArgumentNotValidException
     * @return Error 메시지를 반환
     *//*

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMsg> handleValidationError(MethodArgumentNotValidException ex) {

        List<String> messages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> String.format("%s : { %s } 은 %s",
                        fe.getField(),
                        fe.getRejectedValue(),
                        fe.getDefaultMessage()))
                .toList();

        ErrorMsg error = ErrorMsg.builder()
                .errorMessage(messages)
                .build();

        return ResponseEntity.badRequest()
                .body(error);
    }
}
*/
