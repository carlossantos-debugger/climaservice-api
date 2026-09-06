package com.climaservice.api.service;

import com.climaservice.api.entity.AmbienteNotaFiscal;
import com.climaservice.api.entity.Cliente;
import com.climaservice.api.entity.Empresa;
import com.climaservice.api.entity.Endereco;
import com.climaservice.api.entity.NotaFiscalServico;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/*
 * Monta e assina digitalmente o XML da DPS (Declaração de Prestação de
 * Serviço) enviada ao Sistema Nacional de NFS-e. A estrutura de elementos
 * segue o desenho geral do padrão nacional (DPS > infDPS > prest/toma/serv/
 * valores), mas os nomes e a ordem exata ainda precisam ser conferidos
 * contra o Swagger/XSD ao vivo do ambiente de homologação antes de um envio
 * real — nesta instalação isso nunca foi validado contra o serviço de
 * verdade, por falta de certificado (ver README, seção NFS-e Fase 2).
 */
@Service
public class NfseXmlService {

    public String montarEAssinarDps(NotaFiscalServico nota, PrivateKey chavePrivada, X509Certificate certificado) {

        try {

            Document documento = construirDocumentoDps(nota);

            assinar(documento, chavePrivada, certificado);

            return serializar(documento);

        } catch (Exception exception) {

            throw new IllegalStateException("Falha ao montar/assinar o XML da DPS", exception);
        }
    }

    private Document construirDocumentoDps(NotaFiscalServico nota) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();

        Document documento = builder.newDocument();

        Empresa empresa = nota.getEmpresa();

        Cliente cliente = nota.getOrdemServico().getCliente();

        long idNota = nota.getId() == null ? 0L : nota.getId();

        String idDps = "DPS" + somenteDigitos(empresa.getCpfCnpj()) + String.format("%015d", idNota);

        Element dps = documento.createElement("DPS");

        documento.appendChild(dps);

        Element infDps = documento.createElement("infDPS");

        infDps.setAttribute("Id", idDps);

        infDps.setIdAttribute("Id", true);

        dps.appendChild(infDps);

        adicionarTexto(documento, infDps, "tpAmb", nota.getAmbiente() == AmbienteNotaFiscal.PRODUCAO ? "1" : "2");

        adicionarTexto(documento, infDps, "dhEmi", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        Element prest = documento.createElement("prest");

        infDps.appendChild(prest);

        adicionarTexto(documento, prest, "CNPJ", somenteDigitos(empresa.getCpfCnpj()));

        adicionarTexto(documento, prest, "xNome", empresa.getNome());

        adicionarEndereco(documento, prest, empresa.getEndereco());

        Element toma = documento.createElement("toma");

        infDps.appendChild(toma);

        String documentoTomador = somenteDigitos(cliente.getCpfCnpj());

        adicionarTexto(documento, toma, documentoTomador.length() > 11 ? "CNPJ" : "CPF", documentoTomador);

        adicionarTexto(documento, toma, "xNome", cliente.getNome());

        adicionarEndereco(documento, toma, cliente.getEndereco());

        Element serv = documento.createElement("serv");

        infDps.appendChild(serv);

        adicionarTexto(documento, serv, "cTribNac", nota.getCodigoServico());

        adicionarTexto(documento, serv, "xDescServ", nota.getDiscriminacaoServico());

        Element valores = documento.createElement("valores");

        infDps.appendChild(valores);

        adicionarTexto(documento, valores, "vServ", nota.getValorServico().toPlainString());

        if (nota.getAliquotaIss() != null) {

            adicionarTexto(documento, valores, "pAliq", nota.getAliquotaIss().toPlainString());
        }

        return documento;
    }

    private void adicionarEndereco(Document documento, Element pai, Endereco endereco) {

        if (endereco == null) {

            return;
        }

        Element end = documento.createElement("end");

        pai.appendChild(end);

        adicionarTexto(documento, end, "xLgr", endereco.getLogradouro());

        adicionarTexto(documento, end, "nro", endereco.getNumero());

        adicionarTexto(documento, end, "xBairro", endereco.getBairro());

        adicionarTexto(documento, end, "cMun", endereco.getCidade());

        adicionarTexto(documento, end, "UF", endereco.getUf());

        adicionarTexto(documento, end, "CEP", somenteDigitos(endereco.getCep()));
    }

    private void adicionarTexto(Document documento, Element pai, String nomeElemento, String valor) {

        if (valor == null) {

            return;
        }

        Element elemento = documento.createElement(nomeElemento);

        elemento.setTextContent(valor);

        pai.appendChild(elemento);
    }

    private String somenteDigitos(String valor) {

        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private void assinar(Document documento, PrivateKey chavePrivada, X509Certificate certificado) throws Exception {

        String idDps = documento.getDocumentElement().getFirstChild().getAttributes().getNamedItem("Id").getNodeValue();

        XMLSignatureFactory fabrica = XMLSignatureFactory.getInstance("DOM");

        Reference referencia = fabrica.newReference("#" + idDps, fabrica.newDigestMethod(DigestMethod.SHA256, null), List.of(fabrica.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);

        SignedInfo signedInfo = fabrica.newSignedInfo(fabrica.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null), fabrica.newSignatureMethod(SignatureMethod.RSA_SHA256, null), List.of(referencia));

        KeyInfoFactory keyInfoFactory = fabrica.getKeyInfoFactory();

        X509Data x509Data = keyInfoFactory.newX509Data(List.of(certificado));

        KeyInfo keyInfo = keyInfoFactory.newKeyInfo(List.of(x509Data));

        DOMSignContext contexto = new DOMSignContext(chavePrivada, documento.getDocumentElement());

        XMLSignature assinatura = fabrica.newXMLSignature(signedInfo, keyInfo);

        assinatura.sign(contexto);
    }

    private String serializar(Document documento) throws Exception {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        StringWriter saida = new StringWriter();

        transformer.transform(new DOMSource(documento), new StreamResult(saida));

        return saida.toString();
    }
}
