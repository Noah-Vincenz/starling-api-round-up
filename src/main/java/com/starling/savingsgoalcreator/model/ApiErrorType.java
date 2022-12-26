package com.starling.savingsgoalcreator.model;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class used for constructing API errors in {@link com.starling.savingsgoalcreator.controller.SavingsGoalCreatorController}.
 */
@Data
@AllArgsConstructor
public class ApiErrorType {
    private HttpStatus status;
    private String message;
    private String code;
}
