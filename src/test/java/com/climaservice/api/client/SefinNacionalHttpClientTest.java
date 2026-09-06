package com.climaservice.api.client;

import com.climaservice.api.entity.AmbienteNotaFiscal;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Testa só o contrato HTTP/JSON/gzip do client contra um servidor HTTP local
 * (sem TLS) — a parte de mTLS já é coberta separadamente em
 * NfseCertificadoServiceTest, e este client aceita o HttpClient já pronto
 * de fora, então não precisa repetir esse handshake aqui.
 */
class SefinNacionalHttpClientTest {

    private HttpServer servidor;

    @AfterEach
    void pararServidor() {

        if (servidor != null) {

            servidor.stop(0);
        }
    }

    @Test
    void deveRetornarAutorizadaQuandoSefinResponde200() throws Exception {

        String xmlRetornado = "<nfse>autorizada</nfse>";

        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);

        servidor.createContext("/nfse", exchange -> {

            String corpo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(corpo.contains("dpsXmlGZipB64"));

            String resposta = "{\"chaveAcesso\":\"35260900000000000000550010000000011000000019\",\"nfseXmlGZipB64\":\"" + gzipBase64(xmlRetornado) + "\"}";

            byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().add("Content-Type", "application/json");

            exchange.sendResponseHeaders(200, bytes.length);

            exchange.getResponseBody().write(bytes);

            exchange.close();
        });

        servidor.start();

        String url = "http://localhost:" + servidor.getAddress().getPort() + "/nfse";

        SefinNacionalHttpClient client = new SefinNacionalHttpClient(url, url);

        EnvioDpsResultado resultado = client.enviarDps(HttpClient.newHttpClient(), "<DPS/>", AmbienteNotaFiscal.HOMOLOGACAO);

        assertTrue(resultado.sucesso());

        assertEquals("35260900000000000000550010000000011000000019", resultado.chaveAcesso());

        assertEquals(xmlRetornado, resultado.nfseXmlRetornado());
    }

    @Test
    void deveRetornarRejeitadaComMensagemQuandoSefinResponde400() throws Exception {

        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);

        servidor.createContext("/nfse", exchange -> {

            String resposta = "{\"mensagem\":\"CNPJ do prestador não habilitado\"}";

            byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(400, bytes.length);

            exchange.getResponseBody().write(bytes);

            exchange.close();
        });

        servidor.start();

        String url = "http://localhost:" + servidor.getAddress().getPort() + "/nfse";

        SefinNacionalHttpClient client = new SefinNacionalHttpClient(url, url);

        EnvioDpsResultado resultado = client.enviarDps(HttpClient.newHttpClient(), "<DPS/>", AmbienteNotaFiscal.HOMOLOGACAO);

        assertFalse(resultado.sucesso());

        assertEquals("CNPJ do prestador não habilitado", resultado.mensagemErro());
    }

    @Test
    void deveLancarExcecaoQuandoServidorEstaInalcancavel() {

        SefinNacionalHttpClient client = new SefinNacionalHttpClient("http://localhost:1", "http://localhost:1");

        assertThrows(IllegalStateException.class, () -> client.enviarDps(HttpClient.newHttpClient(), "<DPS/>", AmbienteNotaFiscal.HOMOLOGACAO));
    }

    private String gzipBase64(String texto) throws java.io.IOException {

        ByteArrayOutputStream bytesComprimidos = new ByteArrayOutputStream();

        try (GZIPOutputStream gzip = new GZIPOutputStream(bytesComprimidos)) {

            gzip.write(texto.getBytes(StandardCharsets.UTF_8));
        }

        return Base64.getEncoder().encodeToString(bytesComprimidos.toByteArray());
    }
}
