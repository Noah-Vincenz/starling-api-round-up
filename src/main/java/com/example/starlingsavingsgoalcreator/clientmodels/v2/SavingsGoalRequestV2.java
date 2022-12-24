package com.example.starlingsavingsgoalcreator.clientmodels.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
@AllArgsConstructor
public class SavingsGoalRequestV2 {
    private String name;
    private String currency;
}
