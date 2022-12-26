package com.starling.savingsgoalcreator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Configuration
public class RequestContextFilter implements WebFilter {

    /**
     * Filter to add the {@link ServerWebExchange} from the original request to our context for access later.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                    .contextWrite(context -> Context.of(RequestContextHolder.CONTEXT_KEY_SERVER_WEB_EXCHANGE, exchange));
    }
}
