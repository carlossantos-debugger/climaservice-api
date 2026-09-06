package com.climaservice.api.service;

import com.climaservice.api.entity.AmbienteNotaFiscal;
import com.climaservice.api.entity.Cliente;
import com.climaservice.api.entity.Empresa;
import com.climaservice.api.entity.Endereco;
import com.climaservice.api.entity.NotaFiscalServico;
import com.climaservice.api.entity.OrdemServico;
import com.climaservice.api.entity.Orcamento;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NfseXmlServiceTest {

    @TempDir
    static Path tempDir;

    static PrivateKey chavePrivada;
    static X509Certificate certificado;

    @Mock
    private OrdemServico ordemServico;

    @Mock
    private Orcamento orcamento;

    @Mock
    private Empresa empresa;

    @Mock
    private Cliente cliente;

    private final Endereco endereco = new Endereco("Rua Exemplo", "100", null, "Centro", "Brusque", "SC", "88350000");

    private final NfseXmlService service = new NfseXmlService();

    @BeforeAll
    static void carregarCertificadoDeTeste() throws Exception {

        Path caminho = CertificadoTesteFactory.gerar(tempDir);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (var entrada = java.nio.file.Files.newInputStream(caminho)) {

            keyStore.load(entrada, CertificadoTesteFactory.SENHA.toCharArray());
        }

        chavePrivada = (PrivateKey) keyStore.getKey(CertificadoTesteFactory.ALIAS, CertificadoTesteFactory.SENHA.toCharArray());

        certificado = (X509Certificate) keyStore.getCertificate(CertificadoTesteFactory.ALIAS);
    }

    @Test
    void deveMontarEAssinarXmlComAssinaturaValida() throws Exception {

        when(empresa.getCpfCnpj()).thenReturn("12345678000199");

        when(empresa.getNome()).thenReturn("ClimaService Instalações");

        when(empresa.getEndereco()).thenReturn(endereco);

        when(cliente.getCpfCnpj()).thenReturn("98765432100");

        when(cliente.getNome()).thenReturn("Cliente Teste");

        when(cliente.getEndereco()).thenReturn(endereco);

        when(ordemServico.getCliente()).thenReturn(cliente);

        NotaFiscalServico nota = new NotaFiscalServico(ordemServico, orcamento, "Manutenção preventiva", "01.07", new BigDecimal("5.00"), new BigDecimal("1000.00"), new BigDecimal("50.00"), AmbienteNotaFiscal.HOMOLOGACAO, empresa);

        String xmlAssinado = service.montarEAssinarDps(nota, chavePrivada, certificado);

        assertTrue(xmlAssinado.contains("ClimaService Instalações"));

        assertTrue(xmlAssinado.contains("Signature"));

        assertTrue(validarAssinatura(xmlAssinado));
    }

    private boolean validarAssinatura(String xml) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        Document documento = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

        /*
         * Um DocumentBuilder genérico não sabe, sem DTD/XSD, que o atributo
         * "Id" é do tipo ID — sem isso getElementById (usado pela resolução
         * da referência "#..." da assinatura) não encontra o elemento.
         * Registra manualmente só para permitir a validação neste teste.
         */
        org.w3c.dom.Element infDps = (org.w3c.dom.Element) documento.getDocumentElement().getFirstChild();

        infDps.setIdAttribute("Id", true);

        var nos = documento.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature");

        DOMValidateContext contexto = new DOMValidateContext(certificado.getPublicKey(), nos.item(0));

        XMLSignature assinatura = XMLSignatureFactory.getInstance("DOM").unmarshalXMLSignature(contexto);

        return assinatura.validate(contexto);
    }
}
