package com.starling.savingsgoalcreator.config;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class WebClientConfiguration {

    @Value("${starling.base-url}")
    private String starlingBaseUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                        .baseUrl(starlingBaseUrl)
                        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .filter(ExchangeFilterFunction.ofRequestProcessor(WebClientConfiguration::addBearerTokenAuth))
                        .build();
    }

    /**
     * For any outgoing request add the bearer access token from the context as Authorization header.
     * @param request the client request to be updated
     * @return the updated client request
     */
    private static Mono<ClientRequest> addBearerTokenAuth(ClientRequest request) {
        return Mono.deferContextual(Mono::just)
                   .flatMap(contextView -> RequestContextHolder.getHttpHeaders())
                   .map(headersFromContext -> ClientRequest.from(request)
                                                           .headers(currentHeaders -> currentHeaders.add(HttpHeaders.AUTHORIZATION, Objects.requireNonNull(headersFromContext.get(HttpHeaders.AUTHORIZATION)).get(0)))
                                                           .build());
    }
}
