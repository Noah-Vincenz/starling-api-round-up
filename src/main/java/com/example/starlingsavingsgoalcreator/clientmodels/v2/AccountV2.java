package com.example.starlingsavingsgoalcreator.clientmodels.v2;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
public class AccountV2 {
    private UUID accountUid;
    private String accountType;
    private UUID defaultCategory;
    private String currency;
    private OffsetDateTime createdAt;
    private String name;
}
