package com.starling.savingsgoalcreator.exception;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class ApiRestRunTimeErrorException extends RuntimeException {
    private final String message;
    private final HttpStatus status;
    private final Throwable cause;

    public ApiRestRunTimeErrorException(String message, HttpStatus status) {
        this(message, status, null);
    }

    public ApiRestRunTimeErrorException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public ApiRestRunTimeErrorException(String message, HttpStatus status, Throwable cause) {
        super(message);
        this.message = message;
        this.cause = cause;
        this.status = status;
    }
}
