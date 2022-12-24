package com.starling.savingsgoalcreator.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;

import com.starling.savingsgoalcreator.model.BearerToken;

import lombok.Getter;

public class ClientRequestInterceptor implements HandlerInterceptor {

    @Getter
    private final BearerToken bearerToken;

    public ClientRequestInterceptor(BearerToken token) {
        this.bearerToken = token;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        final String authorizationHeaderValue = request.getHeader("Authorization");
        if (authorizationHeaderValue != null && authorizationHeaderValue.startsWith("Bearer")) {
            String accessToken = authorizationHeaderValue.substring(7);
            bearerToken.setToken(accessToken);
        }
        return true;
    }
}
