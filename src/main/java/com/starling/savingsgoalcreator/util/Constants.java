package com.starling.savingsgoalcreator.util;

public final class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String ERROR_MSG_AUTHORIZATION_HEADER_NOT_SET = "Authorization bearer token header must be set";
    public static final String ERROR_MSG_MANDATORY_FIELD_IN_REQUEST = "Field [%s] is mandatory in the request body";
    public static final String ERROR_MSG_INVALID_CURRENCY = "Field [currency] is not a valid ISO 4217 code";
    public static final String ERROR_MSG_MANDATORY_QUERY_PARAMETERS = "Query parameters [minDate] and [maxDate] are mandatory";
    public static final String ERROR_MSG_QUERY_PARAMETERS_WRONG_FORMAT = "Query parameters [minDate] and [maxDate] must be in the format yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
}
