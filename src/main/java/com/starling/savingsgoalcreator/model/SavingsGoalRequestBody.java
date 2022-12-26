package com.starling.savingsgoalcreator.model;

import lombok.Data;

/**
 * Body used for incoming requests for {@link com.starling.savingsgoalcreator.controller.SavingsGoalCreatorController}.
 */
@Data
public class SavingsGoalRequestBody {
    String savingsGoalName;
}
