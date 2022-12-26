package com.starling.savingsgoalcreator.model;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SavingsGoalCreationApiResponseError {
    private HttpStatus starlingErrorStatus;
    private String message;
}
