package com.starling.savingsgoalcreator.util;

import java.util.Currency;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import com.starling.savingsgoalcreator.exception.ApiRestRunTimeErrorException;
import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiRequestBody;

import static com.starling.savingsgoalcreator.util.Constants.ERROR_MSG_AUTHORIZATION_HEADER_NOT_SET;
import static com.starling.savingsgoalcreator.util.Constants.ERROR_MSG_INVALID_CURRENCY;
import static com.starling.savingsgoalcreator.util.Constants.ERROR_MSG_MANDATORY_FIELD_IN_REQUEST;
import static com.starling.savingsgoalcreator.util.Constants.ERROR_MSG_MANDATORY_QUERY_PARAMETERS;
import static com.starling.savingsgoalcreator.util.Constants.ERROR_MSG_QUERY_PARAMETERS_WRONG_FORMAT;
import reactor.core.publisher.Mono;

/**
 * Class to validate incoming API requests.
 */
public final class RequestValidator {

    private static final String QUERY_PARAM_MIN_DATE = "minDate";
    private static final String QUERY_PARAM_MAX_DATE = "maxDate";
    private static final String TIMESTAMP_FORMAT = "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\dZ";

    private RequestValidator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validate a given request. The Authorization header with a bearer token is mandatory.
     */
    public static Mono<Void> validateRequest(SavingsGoalCreationApiRequestBody requestBody) {
        return RequestContextHolder.getServerWebExchange()
                                   .flatMap(exchange -> validateRequestAuthorizationHeader(exchange.getRequest().getHeaders())
                                           .then(validateRequestQueryParams(exchange.getRequest().getQueryParams().toSingleValueMap()))
                                           .then(validateRequestBody(requestBody)));
    }

    /**
     * Validate a given request's authorization header.
     */
    private static Mono<Void> validateRequestAuthorizationHeader(HttpHeaders headers) {
        // if Authorization header is empty or not set to be a Bearer token value
        if (CollectionUtils.isEmpty(headers.get(HttpHeaders.AUTHORIZATION)) ||
            !headers.get(HttpHeaders.AUTHORIZATION).get(0).contains("Bearer ")) {
            return Mono.error(new ApiRestRunTimeErrorException(ERROR_MSG_AUTHORIZATION_HEADER_NOT_SET, HttpStatus.UNAUTHORIZED));
        }
        return Mono.empty();
    }

    /**
     * Validate a given request's query parameters.
     */
    private static Mono<Void> validateRequestQueryParams(Map<String, String> queryParams) {
        if (queryParams.size() != 2 ||
            !queryParams.containsKey(QUERY_PARAM_MIN_DATE) ||
            !queryParams.containsKey(QUERY_PARAM_MAX_DATE)) {
            return Mono.error(new ApiRestRunTimeErrorException(ERROR_MSG_MANDATORY_QUERY_PARAMETERS, HttpStatus.BAD_REQUEST));
        }
        if (!GenericValidator.matchRegexp(queryParams.get(QUERY_PARAM_MIN_DATE), TIMESTAMP_FORMAT) ||
            !GenericValidator.matchRegexp(queryParams.get(QUERY_PARAM_MAX_DATE), TIMESTAMP_FORMAT)) {
            return Mono.error(new ApiRestRunTimeErrorException(ERROR_MSG_QUERY_PARAMETERS_WRONG_FORMAT, HttpStatus.BAD_REQUEST));
        }
        return Mono.empty();
    }

    /**
     * Validate a given request body.
     */
    private static Mono<Void> validateRequestBody(SavingsGoalCreationApiRequestBody requestBody) {
        if (StringUtils.isEmpty(requestBody.getSavingsGoalName())) {
            return Mono.error(new ApiRestRunTimeErrorException(String.format(ERROR_MSG_MANDATORY_FIELD_IN_REQUEST, "savingsGoalName"), HttpStatus.BAD_REQUEST));
        }
        if (StringUtils.isEmpty(requestBody.getCurrency())) {
            return Mono.error(new ApiRestRunTimeErrorException(String.format(ERROR_MSG_MANDATORY_FIELD_IN_REQUEST, "currency"), HttpStatus.BAD_REQUEST));
        } else {
            try {
                Currency.getInstance(requestBody.getCurrency());
            } catch (IllegalArgumentException e) { // currency code is not a supported ISO 4217 code
                return Mono.error(new ApiRestRunTimeErrorException(ERROR_MSG_INVALID_CURRENCY, HttpStatus.BAD_REQUEST));
            }
        }
        return Mono.empty();
    }
}
