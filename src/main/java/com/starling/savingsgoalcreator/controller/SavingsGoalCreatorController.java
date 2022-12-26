package com.starling.savingsgoalcreator.controller;

import java.util.UUID;

import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiResponse;
import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiResponseError;
import com.starling.savingsgoalcreator.model.SavingsGoalRequestBody;
import com.starling.savingsgoalcreator.service.SavingsGoalCreatorService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/api")
@RequiredArgsConstructor
@Slf4j
public class SavingsGoalCreatorController {

    private final SavingsGoalCreatorService service;

    /**
     * For each user account, create a savings goal with given name for all transactions between the minDate and maxDate timestamps.
     */
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
    public Flux<SavingsGoalCreationApiResponse> createSavingsGoal(
            @Valid @RequestBody SavingsGoalRequestBody savingsGoalRequestBody,
            @ApiParam("__Mandatory__. Used transactions will be on or after this date (yyyy-MM-dd'T'HH:mm:ss.SSSZ).") @Valid @RequestParam(value = "minDate") String minDate,
            @ApiParam("__Mandatory__. Used transactions will be on or before this date (yyyy-MM-dd'T'HH:mm:ss.SSSZ).") @Valid @RequestParam(value = "maxDate") String maxDate) {
        return service.createSavingsGoal(savingsGoalRequestBody.getSavingsGoalName(), minDate, maxDate);
    }


    /**
     * For a given user account, create a savings goal with given name for all transactions between the minDate and maxDate timestamps.
     */
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
    public Mono<SavingsGoalCreationApiResponse> createSavingsGoalForGivenAccount(
            @Valid @RequestBody SavingsGoalRequestBody savingsGoalRequestBody,
            @ApiParam("__Mandatory__. Used transactions will be on or after this date (yyyy-MM-dd'T'HH:mm:ss.SSSZ).") @Valid @RequestParam(value = "minDate") String minDate,
            @ApiParam("__Mandatory__. Used transactions will be on or before this date (yyyy-MM-dd'T'HH:mm:ss.SSSZ).") @Valid @RequestParam(value = "maxDate") String maxDate,
            @ApiParam(value = "The account id")
            @PathVariable(value = "accountId") UUID accountId) {
        return service.createSavingsGoal(accountId, savingsGoalRequestBody.getSavingsGoalName(), minDate, maxDate);
    }
}
