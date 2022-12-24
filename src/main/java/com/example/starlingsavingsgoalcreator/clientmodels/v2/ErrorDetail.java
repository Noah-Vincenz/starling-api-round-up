package com.example.starlingsavingsgoalcreator.clientmodels.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Data
public class ErrorDetail {
    private String message;
}
