package com.climaservice.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import java.io.FileInputStream;
import java.net.http.HttpClient;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Enumeration;

/*
 * Carrega o certificado digital A1 (arquivo PKCS#12) usado para autenticação
 * mTLS com o Sistema Nacional de NFS-e. Enquanto
 * NOTA_FISCAL_CERTIFICADO_CAMINHO não estiver configurado — o caso desta
 * instalação, que não possui certificado ICP-Brasil vinculado ao CNPJ da
 * empresa — isConfigurado() retorna false e nenhuma tentativa de conexão
 * real é feita (ver NotaFiscalServicoService.enviar).
 */
@Service
public class NfseCertificadoService {

    private final String caminho;
    private final char[] senha;

    private KeyStore keyStore;
    private String alias;

    public NfseCertificadoService(@Value("${app.nota-fiscal.certificado.caminho:}") String caminho, @Value("${app.nota-fiscal.certificado.senha:}") String senha) {

        this.caminho = caminho;
        this.senha = senha == null ? new char[0] : senha.toCharArray();
    }

    public boolean isConfigurado() {

        return caminho != null && !caminho.isBlank();
    }

    public HttpClient obterClienteHttp() {

        carregarSeNecessario();

        try {

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

            keyManagerFactory.init(keyStore, senha);

            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            return HttpClient.newBuilder().sslContext(sslContext).connectTimeout(Duration.ofSeconds(30)).build();

        } catch (Exception exception) {

            throw new IllegalStateException("Falha ao configurar cliente mTLS com o certificado digital configurado", exception);
        }
    }

    public PrivateKey obterChavePrivada() {

        carregarSeNecessario();

        try {

            return (PrivateKey) keyStore.getKey(alias, senha);

        } catch (Exception exception) {

            throw new IllegalStateException("Falha ao obter a chave privada do certificado digital configurado", exception);
        }
    }

    public X509Certificate obterCertificado() {

        carregarSeNecessario();

        try {

            return (X509Certificate) keyStore.getCertificate(alias);

        } catch (Exception exception) {

            throw new IllegalStateException("Falha ao obter o certificado digital configurado", exception);
        }
    }

    private void carregarSeNecessario() {

        if (!isConfigurado()) {

            throw new IllegalStateException("Certificado digital não configurado nesta instalação");
        }

        if (keyStore != null) {

            return;
        }

        try (FileInputStream entrada = new FileInputStream(caminho)) {

            KeyStore ks = KeyStore.getInstance("PKCS12");

            ks.load(entrada, senha);

            Enumeration<String> aliases = ks.aliases();

            if (!aliases.hasMoreElements()) {

                throw new IllegalStateException("O arquivo de certificado configurado não contém nenhuma chave");
            }

            this.alias = aliases.nextElement();

            this.keyStore = ks;

        } catch (Exception exception) {

            throw new IllegalStateException("Falha ao carregar o certificado digital configurado em " + caminho, exception);
        }
    }
}
