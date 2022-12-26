package com.starling.savingsgoalcreator.model;

import java.util.UUID;

import com.starling.savingsgoalcreator.clientmodels.v2.SavingsGoalsV2;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SavingsGoalCreationApiResponse {
    private UUID accountId;
    private boolean savingsGoalCreated;
    private String message;
    private int savingsGoalsCount;
    private SavingsGoalsV2 currentSavingsGoals;
}
