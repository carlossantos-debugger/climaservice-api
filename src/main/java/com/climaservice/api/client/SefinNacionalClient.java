package com.climaservice.api.client;

import com.climaservice.api.entity.AmbienteNotaFiscal;

import java.net.http.HttpClient;

/*
 * Abstrai o envio de uma DPS assinada ao Sistema Nacional de NFS-e (Sefin
 * Nacional). Recebe o HttpClient já configurado com mTLS pelo chamador —
 * este client não sabe nada sobre certificados, só sobre o contrato HTTP.
 */
public interface SefinNacionalClient {

    EnvioDpsResultado enviarDps(HttpClient clienteHttp, String xmlAssinado, AmbienteNotaFiscal ambiente);
}
