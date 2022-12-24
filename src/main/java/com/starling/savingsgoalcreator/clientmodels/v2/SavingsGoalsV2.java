package com.starling.savingsgoalcreator.clientmodels.v2;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
public class SavingsGoalsV2 {
    private List<SavingsGoalV2> savingsGoalList;
}
