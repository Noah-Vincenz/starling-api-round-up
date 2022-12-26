package com.starling.savingsgoalcreator.exception;

import com.starling.savingsgoalcreator.model.ApiErrorType;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ApiRestRunTimeErrorException extends RuntimeException {
    private ApiErrorType errorType;
    private Object[] args;
    private Throwable cause;

    public ApiRestRunTimeErrorException(ApiErrorType apiErrorType, Object... args) {
        this(apiErrorType, (Throwable)null, args);
    }

    public ApiRestRunTimeErrorException(ApiErrorType apiErrorType, Throwable cause, Object... args) {
        super(String.format(apiErrorType.getMessage(), args));
        this.errorType = apiErrorType;
        this.cause = cause;
        this.args = args;
    }
}
