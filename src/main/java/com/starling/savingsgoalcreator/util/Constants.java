package com.starling.savingsgoalcreator.util;

public final class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String ERROR_MSG_AUTHORIZATION_HEADER_NOT_SET = "Authorization bearer token header must be set";
    public static final String ERROR_MSG_MANDATORY_FIELD_IN_REQUEST = "Field [%s] is mandatory in the request body";
    public static final String ERROR_MSG_INVALID_CURRENCY = "Field [currency] is not a valid ISO 4217 code";
}
