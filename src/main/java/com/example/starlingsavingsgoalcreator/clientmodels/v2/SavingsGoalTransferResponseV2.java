package com.example.starlingsavingsgoalcreator.clientmodels.v2;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
public class SavingsGoalTransferResponseV2 {
    private UUID transferUid;
    private boolean success;
    private List<ErrorDetail> errors;
}
