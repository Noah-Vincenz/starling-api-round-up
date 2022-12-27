package com.starling.savingsgoalcreator.controller;

import java.util.UUID;

import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.starling.savingsgoalcreator.exception.ApiRestRunTimeErrorException;
import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiRequestBody;
import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiResponse;
import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiResponseError;
import com.starling.savingsgoalcreator.service.SavingsGoalCreatorService;

import static com.starling.savingsgoalcreator.util.RequestValidator.validateRequest;
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
     * For each user account, create a savings goal with given name and currency for all transactions between the minDate and maxDate timestamps.
     */
    @ApiOperation(value = "Create Savings Goal", nickname = "createSavingsGoal", notes = "Used to create a newly named savings goal for transactions across all user accounts in a given time window.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 400, message = "Bad Request", response = SavingsGoalCreationApiResponseError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SavingsGoalCreationApiResponseError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = SavingsGoalCreationApiResponseError.class)
    })
    @PutMapping(
            path = "/savings-goals",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Flux<SavingsGoalCreationApiResponse> createSavingsGoal(
            @Valid @RequestBody SavingsGoalCreationApiRequestBody requestBody,
            @ApiParam("__Mandatory__. Used transactions will be on or after this date (yyyy-MM-dd'T'HH:mm:ss.SSSZ).") @Valid @RequestParam(value = "minDate", required=false) String minDate,
            @ApiParam("__Mandatory__. Used transactions will be on or before this date (yyyy-MM-dd'T'HH:mm:ss.SSSZ).") @Valid @RequestParam(value = "maxDate", required=false) String maxDate) {
        return validateRequest(requestBody).thenMany(service.createSavingsGoal(requestBody, minDate, maxDate))
                                           .doOnError(throwable -> {
                                               String errorMessage = "Failed to create savings goal";
                                               log.error(errorMessage);
                                               throw new ApiRestRunTimeErrorException(errorMessage, throwable);
                                           });
    }


    /**
     * For a given user account, create a savings goal with given name and currency for all transactions between the minDate and maxDate timestamps.
     */
    @ApiOperation(value = "Create Savings Goal For Given Account", nickname = "createSavingsGoalForGivenAccount", notes = "Used to create a newly named savings goal for a given account's transactions in a given time window.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 400, message = "Bad Request", response = SavingsGoalCreationApiResponseError.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = SavingsGoalCreationApiResponseError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = SavingsGoalCreationApiResponseError.class)
    })
    @PutMapping(
            path = "/savings-goals/{accountId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<SavingsGoalCreationApiResponse> createSavingsGoalForGivenAccount(
            @Valid @RequestBody SavingsGoalCreationApiRequestBody requestBody,
            @ApiParam("__Mandatory__. Used transactions will be on or after this date (yyyy-MM-dd'T'HH:mm:ss.SSSZ).") @Valid @RequestParam(value = "minDate", required=false) String minDate,
            @ApiParam("__Mandatory__. Used transactions will be on or before this date (yyyy-MM-dd'T'HH:mm:ss.SSSZ).") @Valid @RequestParam(value = "maxDate", required=false) String maxDate,
            @ApiParam(value = "The account id")
            @PathVariable(value = "accountId") UUID accountId) {
        return validateRequest(requestBody).then(service.createSavingsGoal(accountId, requestBody, minDate, maxDate))
                                           .doOnError(throwable -> {
                                               String errorMessage = String.format("Failed to create savings goal for account [%s]", accountId);
                                               log.error(errorMessage);
                                               throw new ApiRestRunTimeErrorException(errorMessage, throwable);
                                           });
    }
}
