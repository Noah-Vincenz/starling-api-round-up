package com.starling.savingsgoalcreator.clientmodels.v2;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
public class BatchPaymentDetails {
    private UUID batchPaymentUid;
    private String batchPaymentType;
}
