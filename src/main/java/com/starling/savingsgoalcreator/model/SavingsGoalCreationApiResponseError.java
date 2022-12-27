package com.starling.savingsgoalcreator.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavingsGoalCreationApiResponseError {
    private String message;
    private UpstreamErrorType upstreamError;
}
