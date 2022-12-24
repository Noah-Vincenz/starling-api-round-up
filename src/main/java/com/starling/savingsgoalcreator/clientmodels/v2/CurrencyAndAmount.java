package com.starling.savingsgoalcreator.clientmodels.v2;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
@AllArgsConstructor
public class CurrencyAndAmount {
    private String currency;
    private Long minorUnits; // int64 in API spec, which has the same max value as Java long

    /**
     * Needed, because without it we get a Jackson exception because of the Long type
     */
    @JsonCreator
    private CurrencyAndAmount(Map<String,Object> properties) {
        this.currency = (String) properties.get("currency");
        try {
            this.minorUnits = (Long) properties.get("minorUnits");
        } catch (ClassCastException e) {
            this.minorUnits = ((Integer) properties.get("minorUnits")).longValue();
        }
    }
}
