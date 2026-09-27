package com.ahmtcnmn.manager.config;


import com.ahmtcnmn.manager.service.bucket4j.LoginRateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ahmtcnmn.manager.service.ApiKey.ApiKeyInterceptor;
import com.ahmtcnmn.manager.service.bucket4j.RateLimitInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final LoginRateLimitInterceptor loginRateLimitInterceptor;
    private final ApiKeyInterceptor apiKeyInterceptor;

    

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/agents/heartbeat", "/api/events/**", "/api/commands/**")
                .order(1);   // ApiKeyInterceptor'dan ÖNCE çalışsın (daha ucuz bir kontrol, gereksiz DB sorgusunu önler)

        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/api/agents/heartbeat", "/api/events/**", "/api/commands/**")
                .order(2);

        registry.addInterceptor(loginRateLimitInterceptor)
                .addPathPatterns("/api/auth/login")
                .order(1);
}
}