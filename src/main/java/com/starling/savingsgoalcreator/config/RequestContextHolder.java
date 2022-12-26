package com.starling.savingsgoalcreator.config;

import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class RequestContextHolder {

    public static final Class<ServerWebExchange> CONTEXT_KEY_SERVER_WEB_EXCHANGE = ServerWebExchange.class;

    private RequestContextHolder() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get the {@link org.springframework.http.HttpHeaders} from the current context.
     * @return the HTTP headers object.
     */
    public static Mono<HttpHeaders> getHttpHeaders() {
        return getServerWebExchange().map(exchange -> exchange.getRequest().getHeaders());
    }

    /**
     * Get the {@link ServerWebExchange} from the current context.
     * @return the current exchange object.
     */
    private static Mono<ServerWebExchange> getServerWebExchange() {
        return Mono.deferContextual(Mono::just)
                   .map(contextView -> contextView.get(CONTEXT_KEY_SERVER_WEB_EXCHANGE));
    }
}
