package com.climaservice.api.integration;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Verifica que app.cors.allowed-origins (padrão local:
 * http://localhost:4200, o servidor de dev do futuro frontend Angular)
 * está de fato ligado na cadeia de segurança, e que uma origem não
 * configurada não recebe o cabeçalho de liberação.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigurationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devePermitirOrigemConfigurada() throws Exception {

        mockMvc.perform(get("/actuator/health").header(HttpHeaders.ORIGIN, "http://localhost:4200")).andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
    }

    @Test
    void naoDeveLiberarOrigemNaoConfigurada() throws Exception {

        mockMvc.perform(get("/actuator/health").header(HttpHeaders.ORIGIN, "http://evil.example.com")).andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
