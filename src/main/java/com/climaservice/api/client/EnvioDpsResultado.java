package com.climaservice.api.client;

/*
 * Resultado do envio de uma DPS ao Sistema Nacional de NFS-e. sucesso=true
 * traz chaveAcesso/nfseXmlRetornado; sucesso=false traz mensagemErro com o
 * motivo relatado pela API (rejeição de negócio, não uma falha de infra —
 * essa continua sendo lançada como exceção pelo client).
 */
public record EnvioDpsResultado(boolean sucesso, String chaveAcesso, String nfseXmlRetornado, String mensagemErro) {

    public static EnvioDpsResultado autorizada(String chaveAcesso, String nfseXmlRetornado) {

        return new EnvioDpsResultado(true, chaveAcesso, nfseXmlRetornado, null);
    }

    public static EnvioDpsResultado rejeitada(String mensagemErro) {

        return new EnvioDpsResultado(false, null, null, mensagemErro);
    }
}
