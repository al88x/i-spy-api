package com.techswitch.ispy.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class HandleException {


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handle(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException) {
            Map<String, String> errors = new HashMap<>();
            ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().forEach((error) -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });

            return ResponseEntity.badRequest().body(errors);
        }
        if (ex instanceof HttpMessageNotReadableException) {
            System.out.println(ex.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("Error", "Bad Request"));
        }
        System.out.println(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("Error", "Internal Server Error"));
    }
}
