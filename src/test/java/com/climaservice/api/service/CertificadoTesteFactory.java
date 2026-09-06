package com.climaservice.api.service;

import java.nio.file.Path;

/*
 * Gera um certificado PKCS#12 autoassinado e descartável, só para os testes
 * de mTLS/assinatura XML — nunca um certificado ICP-Brasil de verdade. Usa o
 * keytool do próprio JDK (evita depender de uma lib externa de geração de
 * certificado só para isso).
 */
final class CertificadoTesteFactory {

    static final String SENHA = "senhaTeste123";
    static final String ALIAS = "teste";

    private CertificadoTesteFactory() {
    }

    static Path gerar(Path diretorio) throws Exception {

        Path caminho = diretorio.resolve("certificado-teste.p12");

        String keytool = System.getProperty("java.home") + java.io.File.separator + "bin" + java.io.File.separator + "keytool";

        ProcessBuilder processBuilder = new ProcessBuilder(keytool, "-genkeypair", "-alias", ALIAS, "-keyalg", "RSA", "-keysize", "2048", "-validity", "30", "-keystore", caminho.toString(), "-storetype", "PKCS12", "-storepass", SENHA, "-keypass", SENHA, "-dname", "CN=Teste ClimaService, OU=Teste, O=Teste, L=Brusque, ST=SC, C=BR");

        processBuilder.redirectErrorStream(true);

        Process processo = processBuilder.start();

        String saida = new String(processo.getInputStream().readAllBytes());

        int codigo = processo.waitFor();

        if (codigo != 0) {

            throw new IllegalStateException("Falha ao gerar certificado de teste via keytool: " + saida);
        }

        return caminho;
    }
}
