package com.starling.savingsgoalcreator.clientmodels.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
@AllArgsConstructor
public class TopUpRequestV2 {
    private CurrencyAndAmount amount;
}
