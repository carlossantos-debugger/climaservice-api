package com.climaservice.api.client;

import com.climaservice.api.entity.AmbienteNotaFiscal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/*
 * Implementação real do envio ao Sefin Nacional: XML da DPS -> gzip -> base64
 * -> POST JSON {"dpsXmlGZipB64": "..."}. O contrato exato de request/response
 * (nomes de campo, formato de erro) foi levantado por pesquisa pública sobre
 * o padrão NFS-e Nacional, não confirmado ao vivo contra o Swagger oficial
 * por falta de certificado digital nesta instalação — ver README.
 */
@Component
public class SefinNacionalHttpClient implements SefinNacionalClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String urlHomologacao;
    private final String urlProducao;

    public SefinNacionalHttpClient(@Value("${app.nota-fiscal.sefin.url-homologacao}") String urlHomologacao, @Value("${app.nota-fiscal.sefin.url-producao}") String urlProducao) {

        this.urlHomologacao = urlHomologacao;
        this.urlProducao = urlProducao;
    }

    @Override
    public EnvioDpsResultado enviarDps(HttpClient clienteHttp, String xmlAssinado, AmbienteNotaFiscal ambiente) {

        try {

            String corpo = objectMapper.writeValueAsString(new RequisicaoEnvioDps(gzipBase64(xmlAssinado)));

            HttpRequest requisicao = HttpRequest.newBuilder().uri(URI.create(ambiente == AmbienteNotaFiscal.PRODUCAO ? urlProducao : urlHomologacao)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8)).build();

            HttpResponse<String> resposta = clienteHttp.send(requisicao, HttpResponse.BodyHandlers.ofString());

            if (resposta.statusCode() >= 200 && resposta.statusCode() < 300) {

                JsonNode corpoResposta = objectMapper.readTree(resposta.body());

                String chaveAcesso = textoOuNulo(corpoResposta, "chaveAcesso");

                String nfseXmlGZipB64 = textoOuNulo(corpoResposta, "nfseXmlGZipB64");

                String nfseXml = nfseXmlGZipB64 != null ? ungzipBase64(nfseXmlGZipB64) : null;

                return EnvioDpsResultado.autorizada(chaveAcesso, nfseXml);
            }

            return EnvioDpsResultado.rejeitada(extrairMensagemErro(resposta));

        } catch (java.io.IOException | InterruptedException exception) {

            if (exception instanceof InterruptedException) {

                Thread.currentThread().interrupt();
            }

            throw new IllegalStateException("Falha de comunicação com o Sistema Nacional de NFS-e", exception);
        }
    }

    private String extrairMensagemErro(HttpResponse<String> resposta) {

        try {

            JsonNode corpo = objectMapper.readTree(resposta.body());

            for (String campo : new String[]{"mensagem", "message", "erro", "detail", "title"}) {

                String valor = textoOuNulo(corpo, campo);

                if (valor != null) {

                    return valor;
                }
            }

        } catch (Exception ignorada) {

            // corpo não é JSON — cai para a mensagem genérica abaixo
        }

        return "Sistema Nacional de NFS-e recusou o envio (HTTP " + resposta.statusCode() + ")";
    }

    private String textoOuNulo(JsonNode no, String campo) {

        JsonNode valor = no.get(campo);

        return valor == null || valor.isNull() ? null : valor.asText();
    }

    private String gzipBase64(String texto) throws java.io.IOException {

        ByteArrayOutputStream bytesComprimidos = new ByteArrayOutputStream();

        try (GZIPOutputStream gzip = new GZIPOutputStream(bytesComprimidos)) {

            gzip.write(texto.getBytes(StandardCharsets.UTF_8));
        }

        return Base64.getEncoder().encodeToString(bytesComprimidos.toByteArray());
    }

    private String ungzipBase64(String base64GZip) throws java.io.IOException {

        byte[] bytesComprimidos = Base64.getDecoder().decode(base64GZip);

        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytesComprimidos))) {

            return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private record RequisicaoEnvioDps(String dpsXmlGZipB64) {
    }
}
