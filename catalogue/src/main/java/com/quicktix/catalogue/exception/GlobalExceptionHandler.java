package com.quicktix.catalogue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.quicktix.catalogue.dto.ErrorResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        // -------------------------------------------
        // 404 — Resource Not Found
        // -------------------------------------------
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
                return new ResponseEntity<>(
                                new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()),
                                HttpStatus.NOT_FOUND);
        }

        // -------------------------------------------
        // 409 — Duplicate Resource
        // -------------------------------------------
        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
                return new ResponseEntity<>(
                                new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value()),
                                HttpStatus.CONFLICT);
        }

        // -------------------------------------------
        // 400 — Bad Request
        // -------------------------------------------
        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
                return new ResponseEntity<>(
                                new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()),
                                HttpStatus.BAD_REQUEST);
        }

        // -------------------------------------------
        // 500 — Internal Server Error (fallback)
        // -------------------------------------------
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
                return new ResponseEntity<>(
                                new ErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value()),
                                HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // -------------------------------------------
        // 400 — Constraint Violation
        // -------------------------------------------
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
                return new ResponseEntity<>(
                                new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()),
                                HttpStatus.BAD_REQUEST);
        }

        // -------------------------------------------
        // 400 — Method Argument Not Valid
        // -------------------------------------------
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleMethodValidation(MethodArgumentNotValidException ex) {
                String error = ex.getBindingResult()
                                .getFieldErrors()
                                .get(0)
                                .getDefaultMessage();

                return new ResponseEntity<>(
                                new ErrorResponse(error, HttpStatus.BAD_REQUEST.value()),
                                HttpStatus.BAD_REQUEST);
        }

        // -------------------------------------------
        // 404 — No Static Resource / Handler Found
        // -------------------------------------------
        @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
        public ResponseEntity<ErrorResponse> handleNoResourceFound(
                        org.springframework.web.servlet.resource.NoResourceFoundException ex) {
                return new ResponseEntity<>(
                                new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()),
                                HttpStatus.NOT_FOUND);
        }

}