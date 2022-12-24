package com.example.starlingsavingsgoalcreator.controller;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.starlingsavingsgoalcreator.model.SavingsGoalCreationApiResponse;
import com.example.starlingsavingsgoalcreator.model.SavingsGoalCreationApiResponseError;
import com.example.starlingsavingsgoalcreator.model.SavingsGoalRequestBody;
import com.example.starlingsavingsgoalcreator.service.SavingsGoalCreatorService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/api")
@RequiredArgsConstructor
@Slf4j
public class SavingsGoalCreatorController {

    private final SavingsGoalCreatorService service;

    @ApiOperation(value = "Create Savings Goal", nickname = "createSavingsGoal", notes = "Used to create a newly named savings goal for transactions across all user accounts in a given time window.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 422, message = "Unprocessable Entity", response = SavingsGoalCreationApiResponseError.class)
    })
    @PostMapping(
            path = "/savings-goals",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<SavingsGoalCreationApiResponse>> createSavingsGoal(
            @Valid @RequestBody SavingsGoalRequestBody savingsGoalRequestBody,
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate) {
        return service.createSavingsGoal(savingsGoalRequestBody.getSavingsGoalName(), startDate, endDate);
    }

    @ApiOperation(value = "Create Savings Goal For Given Account", nickname = "createSavingsGoalForGivenAccount", notes = "Used to create a newly named savings goal for a given account's transactions in a given time window.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 422, message = "Unprocessable Entity", response = SavingsGoalCreationApiResponseError.class)
    })
    @PostMapping(
            path = "/savings-goals/{accountId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<SavingsGoalCreationApiResponse>> createSavingsGoalForGivenAccount(
            @Valid @RequestBody SavingsGoalRequestBody savingsGoalRequestBody,
            @RequestParam(value = "startDate") String startDate,
            @RequestParam(value = "endDate") String endDate,
            @ApiParam(value = "The account id")
            @PathVariable(value = "accountId") UUID accountId) {
        return service.createSavingsGoal(accountId, savingsGoalRequestBody.getSavingsGoalName(), startDate, endDate);
    }
}
