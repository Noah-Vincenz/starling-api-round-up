package com.example.starlingsavingsgoalcreator.clientmodels.v2;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavingsGoalV2 {
    private UUID savingsGoalUid;
    private String name;
    private CurrencyAndAmount target;
    private CurrencyAndAmount totalSaved;
    private Integer savedPercentage; // int32 in API spec, which has the same max value as Java Integer
}
