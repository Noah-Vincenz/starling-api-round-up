package com.starling.savingsgoalcreator.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.starling.savingsgoalcreator.exception.ApiRestRunTimeErrorException;
import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiRequestBody;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class RequestValidatorTest {

    @Test
    void test_validateRequestAuthorizationHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer accessToken");

        Mono<Void> response = RequestValidator.validateRequestAuthorizationHeader(headers);

        StepVerifier.create(response)
                    .expectNext()
                    .verifyComplete();
    }

    @Test
    void test_validateRequestAuthorizationHeader_Without_BearerAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        Mono<Void> response = RequestValidator.validateRequestAuthorizationHeader(headers);

        StepVerifier.create(response)
                    .expectErrorMatches(throwable -> {
                        ApiRestRunTimeErrorException exception = (ApiRestRunTimeErrorException) throwable;
                        return exception.getMessage().equals("Authorization bearer token header must be set") &&
                               exception.getStatus().equals(HttpStatus.UNAUTHORIZED);
                    })
                    .verify();
    }

    @Test
    void test_validateRequestQueryParams() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("minDate", "2022-12-01T12:12:12.000Z");
        queryParams.put("maxDate", "2022-12-20T12:12:12.000Z");

        Mono<Void> response = RequestValidator.validateRequestQueryParams(queryParams);

        StepVerifier.create(response)
                    .expectNext()
                    .verifyComplete();
    }

    @Test
    void test_validateRequestQueryParams_Wrong_Format() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("minDate", "2022-12-01T12:12:12.000Z");
        queryParams.put("maxDate", "2022-12-2012:12:12.000Z");

        Mono<Void> response = RequestValidator.validateRequestQueryParams(queryParams);

        StepVerifier.create(response)
                    .expectErrorMatches(throwable -> {
                        ApiRestRunTimeErrorException exception = (ApiRestRunTimeErrorException) throwable;
                        return exception.getMessage().equals("Query parameters [minDate] and [maxDate] must be in the format yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") &&
                               exception.getStatus().equals(HttpStatus.BAD_REQUEST);
                    })
                    .verify();
    }

    @Test
    void test_validateRequestQueryParams_Missing_MaxDate() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("minDate", "2022-12-01T12:12:12.000Z");

        Mono<Void> response = RequestValidator.validateRequestQueryParams(queryParams);

        StepVerifier.create(response)
                    .expectErrorMatches(throwable -> {
                        ApiRestRunTimeErrorException exception = (ApiRestRunTimeErrorException) throwable;
                        return exception.getMessage().equals("Query parameters [minDate] and [maxDate] are mandatory") &&
                               exception.getStatus().equals(HttpStatus.BAD_REQUEST);
                    })
                    .verify();
    }

    @Test
    void test_validateRequestBody() {
        SavingsGoalCreationApiRequestBody requestBody = new SavingsGoalCreationApiRequestBody();
        requestBody.setCurrency("GBP");
        requestBody.setSavingsGoalName("Car");

        Mono<Void> response = RequestValidator.validateRequestBody(requestBody);

        StepVerifier.create(response)
                    .expectNext()
                    .verifyComplete();
    }

    @Test
    void test_validateRequestBody_invalidCurrency() {
        SavingsGoalCreationApiRequestBody requestBody = new SavingsGoalCreationApiRequestBody();
        requestBody.setCurrency("GB");
        requestBody.setSavingsGoalName("Car");

        Mono<Void> response = RequestValidator.validateRequestBody(requestBody);

        StepVerifier.create(response)
                    .expectErrorMatches(throwable -> {
                        ApiRestRunTimeErrorException exception = (ApiRestRunTimeErrorException) throwable;
                        return exception.getMessage().equals("Field [currency] is not a valid ISO 4217 code") &&
                               exception.getStatus().equals(HttpStatus.BAD_REQUEST);
                    })
                    .verify();
    }

    @Test
    void test_validateRequestBody_without_savingsGoalName() {
        SavingsGoalCreationApiRequestBody requestBody = new SavingsGoalCreationApiRequestBody();
        requestBody.setCurrency("GBP");

        Mono<Void> response = RequestValidator.validateRequestBody(requestBody);
        StepVerifier.create(response)
                    .expectErrorMatches(throwable -> {
                        ApiRestRunTimeErrorException exception = (ApiRestRunTimeErrorException) throwable;
                        return exception.getMessage().equals("Field [savingsGoalName] is mandatory in the request body") &&
                               exception.getStatus().equals(HttpStatus.BAD_REQUEST);
                    })
                    .verify();
    }

    @Test
    void test_validateRequestBody_without_currency() {
        SavingsGoalCreationApiRequestBody requestBody = new SavingsGoalCreationApiRequestBody();
        requestBody.setSavingsGoalName("Car");

        Mono<Void> response = RequestValidator.validateRequestBody(requestBody);

        StepVerifier.create(response)
                    .expectErrorMatches(throwable -> {
                        ApiRestRunTimeErrorException exception = (ApiRestRunTimeErrorException) throwable;
                        return exception.getMessage().equals("Field [currency] is mandatory in the request body") &&
                               exception.getStatus().equals(HttpStatus.BAD_REQUEST);
                    })
                    .verify();
    }
}
