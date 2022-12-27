package com.starling.savingsgoalcreator.model;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class used for constructing custom API errors in {@link com.starling.savingsgoalcreator.controller.SavingsGoalCreatorController}.
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpstreamErrorType {
    private HttpStatus status;
    private String message;
    private Integer code;
}
