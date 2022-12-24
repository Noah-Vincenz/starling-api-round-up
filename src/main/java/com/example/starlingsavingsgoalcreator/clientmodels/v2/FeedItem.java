package com.example.starlingsavingsgoalcreator.clientmodels.v2;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
public class FeedItem {
    private UUID feedItemUid;
    private UUID categoryUid;
    private CurrencyAndAmount amount;
    private CurrencyAndAmount sourceAmount;
    private String direction;
    private OffsetDateTime updatedAt;
    private OffsetDateTime transactionTime;
    private OffsetDateTime settlementTime;
    private String source;
    private String status;
    private String counterPartyType;
    private String counterPartyName;
    private String counterPartySubEntityName;
    private String counterPartySubEntityIdentifier;
    private String counterPartySubEntitySubIdentifier;
    private String reference;
    private String country;
    private String spendingCategory;
    private boolean hasAttachment;
    private boolean hasReceipt;
    private BatchPaymentDetails batchPaymentDetails;
}
