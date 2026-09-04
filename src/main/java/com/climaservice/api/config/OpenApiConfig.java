package com.climaservice.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI climaServiceOpenApi() {

        return new OpenAPI().info(new Info().title("ClimaService API").description("API REST para gerenciamento de serviços de climatização e manutenção de ar-condicionado. " + "Multi-tenant: o tenant (empresa) é sempre derivado do usuário autenticado, nunca informado pelo cliente da API. " + "Erros seguem um envelope padrão (ApiErrorResponse/ValidationErrorResponse): 400 para violação de regra de negócio ou " + "validação de entrada, 401 para ausência/invalidade de token, 403 para perfil sem permissão, 404 para recurso inexistente " + "ou pertencente a outro tenant.").version("v1")).addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME)).components(new Components().addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme().name(BEARER_SCHEME_NAME).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
