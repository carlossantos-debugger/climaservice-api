package com.climaservice.api.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NfseCertificadoServiceTest {

    @TempDir
    static Path tempDir;

    static Path certificado;

    @BeforeAll
    static void gerarCertificadoDeTeste() throws Exception {

        certificado = CertificadoTesteFactory.gerar(tempDir);
    }

    @Test
    void deveIndicarNaoConfiguradoQuandoCaminhoEstaVazio() {

        NfseCertificadoService service = new NfseCertificadoService("", "");

        assertFalse(service.isConfigurado());

        assertThrows(IllegalStateException.class, service::obterClienteHttp);
    }

    @Test
    void deveCarregarChavePrivadaECertificadoDoArquivoConfigurado() {

        NfseCertificadoService service = new NfseCertificadoService(certificado.toString(), CertificadoTesteFactory.SENHA);

        assertTrue(service.isConfigurado());

        PrivateKey chavePrivada = service.obterChavePrivada();

        assertNotNull(chavePrivada);

        assertEquals("RSA", chavePrivada.getAlgorithm());

        X509Certificate certificadoCarregado = service.obterCertificado();

        assertNotNull(certificadoCarregado);

        assertTrue(certificadoCarregado.getSubjectX500Principal().getName().contains("Teste ClimaService"));
    }

    @Test
    void deveConstruirClienteHttpConfiguradoComOCertificado() {

        NfseCertificadoService service = new NfseCertificadoService(certificado.toString(), CertificadoTesteFactory.SENHA);

        HttpClient clienteHttp = service.obterClienteHttp();

        assertNotNull(clienteHttp);

        assertTrue(clienteHttp.sslContext() != null);
    }

    @Test
    void deveLancarExcecaoQuandoSenhaEstaErrada() {

        NfseCertificadoService service = new NfseCertificadoService(certificado.toString(), "senhaErrada");

        assertThrows(IllegalStateException.class, service::obterChavePrivada);
    }
}
