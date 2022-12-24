package com.example.starlingsavingsgoalcreator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.starlingsavingsgoalcreator.interceptor.ClientRequestInterceptor;
import com.example.starlingsavingsgoalcreator.model.BearerToken;

@EnableWebMvc
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(clientRequestInterceptor());
    }

    @Bean
    public ClientRequestInterceptor clientRequestInterceptor() {
        return new ClientRequestInterceptor(bearerToken());
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public BearerToken bearerToken() {
        return new BearerToken();
    }

}
