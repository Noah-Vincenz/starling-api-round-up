package com.starling.savingsgoalcreator.model;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SavingsGoalCreationApiResponseError {
    private HttpStatus status;
    private String message;
}
