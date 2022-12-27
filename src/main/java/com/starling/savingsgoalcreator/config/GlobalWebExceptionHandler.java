package com.starling.savingsgoalcreator.config;

import java.util.Map;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starling.savingsgoalcreator.exception.ApiRestRunTimeErrorException;
import com.starling.savingsgoalcreator.model.SavingsGoalCreationApiResponseError;
import com.starling.savingsgoalcreator.model.UpstreamErrorType;

import reactor.core.publisher.Mono;

@Configuration
public class GlobalWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    private final ObjectMapper mapper;

    public GlobalWebExceptionHandler(DefaultErrorAttributes errorAttributes, WebProperties.Resources resources, ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(configurer.getWriters());
        this.mapper = new ObjectMapper();
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * Construct and render a custom error response. This uses the data retrieved from the exception thrown to construct a more meaningful response.
     * @return the constructed response that is returned to the user.
     */
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        HttpStatus mainStatus = HttpStatus.INTERNAL_SERVER_ERROR; // default
        HttpStatus upstreamStatus = null;
        String upstreamMessage = null;
        Integer upstreamCode = null;
        Throwable thrownException = getError(request);
        String mainMessage = thrownException.getMessage();
        if (thrownException instanceof ApiRestRunTimeErrorException) { // exception explicitly thrown by code
            ApiRestRunTimeErrorException apiRestRunTimeErrorException = (ApiRestRunTimeErrorException) getError(request);
            if (apiRestRunTimeErrorException.getCause() != null) { // upstream error exists
                Throwable cause = apiRestRunTimeErrorException.getCause();
                if (cause instanceof WebClientResponseException) { // error happened when hitting Starling API
                    WebClientResponseException webClientResponseException = (WebClientResponseException) cause;
                    upstreamStatus = webClientResponseException.getStatusCode();
                    upstreamMessage = webClientResponseException.getMessage();
                    upstreamCode = webClientResponseException.getRawStatusCode();
                    mainMessage = updateMainMessageForUpstreamError(mainMessage, upstreamStatus);
                } else {
                    RuntimeException runtimeException = (RuntimeException) cause;
                    mainMessage = String.join(". ", mainMessage, runtimeException.getMessage());
                    if (runtimeException instanceof ApiRestRunTimeErrorException) {
                        ApiRestRunTimeErrorException causeApiRestRunTimeErrorException = (ApiRestRunTimeErrorException) runtimeException;
                        mainStatus = causeApiRestRunTimeErrorException.getStatus();
                    }
                }
            }
        }
        var apiResponse = new SavingsGoalCreationApiResponseError(mainMessage,
                                                                  new UpstreamErrorType(upstreamStatus, upstreamMessage, upstreamCode));
        return ServerResponse.status(mainStatus)
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(BodyInserters.fromValue(mapper.convertValue(apiResponse, Map.class)));
    }

    private String updateMainMessageForUpstreamError(String message, HttpStatus upstreamStatus) {
        if (upstreamStatus.equals(HttpStatus.FORBIDDEN)) {
            return String.join(". ", message, "Make sure all mandatory headers are present and the access token provided is valid");
        }
        return message;
    }
}
