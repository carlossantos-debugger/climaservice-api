package com.climaservice.api.integration;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiObservabilityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveExporApenasHealthCheckSemAutenticacao() throws Exception {

        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void deveOcultarDetalhesInternosDoHealthCheck() throws Exception {

        mockMvc.perform(get("/actuator/health")).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("components"))));
    }

    @Test
    void deveExporDocumentacaoOpenApiSemAutenticacao() throws Exception {

        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.openapi").exists()).andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer")).andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http")).andExpect(jsonPath("$.paths./clientes").exists());
    }

    @Test
    void deveRedirecionarParaSwaggerUiSemAutenticacao() throws Exception {

        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
    }
}
