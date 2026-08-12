package com.interviewace.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Configuration
public class OpenApiConfig {
    private static final String BEARER_AUTH = "bearerAuth";
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/health"
    );

    @Bean
    OpenAPI interviewAceOpenApi() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes(BEARER_AUTH, bearerScheme))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    OpenApiCustomizer publicEndpointSecurityCustomizer() {
        return openApi -> PUBLIC_PATHS.forEach(path -> {
            if (openApi.getPaths() != null && openApi.getPaths().get(path) != null) {
                openApi.getPaths().get(path).readOperations().forEach(operation -> operation.setSecurity(List.of()));
            }
        });
    }
}
