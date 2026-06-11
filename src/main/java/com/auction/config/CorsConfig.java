package com.auction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Development/workability:
        // - Allow all origins (patterns)
        // - Do NOT allow credentials, to avoid browser blocking preflight
        // This prevents failures when your browser uses `localhost` vs `127.0.0.1`.
        config.setAllowCredentials(false);
        config.addAllowedOriginPattern("*");

        // Explicitly include OPTIONS (preflight)
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        config.addAllowedHeader("*");

        // Expose important headers
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");

        // Cache preflight request for 1 hour
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
