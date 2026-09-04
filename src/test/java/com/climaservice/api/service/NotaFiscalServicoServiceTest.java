package com.climaservice.api.service;

import com.climaservice.api.dto.NotaFiscalServicoRequestDTO;
import com.climaservice.api.dto.NotaFiscalServicoResponseDTO;
import com.climaservice.api.entity.*;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.NotaFiscalServicoRepository;
import com.climaservice.api.repository.OrcamentoRepository;
import com.climaservice.api.repository.OrdemServicoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotaFiscalServicoServiceTest {

    private static final Long EMPRESA_ID = 8001L;
    private static final String AMBIENTE_PADRAO = "HOMOLOGACAO";

    @Mock
    private NotaFiscalServicoRepository notaFiscalServicoRepository;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private OrcamentoRepository orcamentoRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private Empresa empresa;

    @Mock
    private Cliente cliente;

    @Mock
    private OrdemServico ordemServico;

    @Mock
    private Orcamento orcamento;

    private NotaFiscalServicoService service;

    private final Endereco enderecoCompleto = new Endereco("Rua Exemplo", "100", null, "Centro", "Brusque", "SC", "88350000");

    private void construirService() {

        service = new NotaFiscalServicoService(notaFiscalServicoRepository, ordemServicoRepository, orcamentoRepository, usuarioAutenticadoService, AMBIENTE_PADRAO);
    }

    private void prepararEmpresaAtual() {

        when(usuarioAutenticadoService.obterEmpresaAtual()).thenReturn(empresa);

        when(empresa.getId()).thenReturn(EMPRESA_ID);
    }

    private void prepararCadastroFiscalCompleto() {

        when(empresa.getEndereco()).thenReturn(enderecoCompleto);

        when(empresa.getRegimeTributario()).thenReturn(RegimeTributario.SIMPLES_NACIONAL);

        when(cliente.getEndereco()).thenReturn(enderecoCompleto);
    }

    private void prepararOrdemServicoEncontrada(Long id) {

        when(ordemServicoRepository.findByIdAndEmpresa_Id(id, EMPRESA_ID)).thenReturn(Optional.of(ordemServico));
    }

    /*
     * Além de localizar a OS, já stuba getCliente() — usado pela
     * validação de cadastro fiscal, alcançada apenas quando a
     * criação chega até lá (ou seja, quando há orçamento aprovado).
     */
    private void prepararOrdemServicoValida(Long id) {

        prepararOrdemServicoEncontrada(id);

        when(ordemServico.getCliente()).thenReturn(cliente);
    }

    private void prepararOrcamentoAprovado(Long ordemServicoId, BigDecimal valorTotal) {

        when(orcamento.getStatus()).thenReturn(StatusOrcamento.APROVADO);

        when(orcamento.getValorTotal()).thenReturn(valorTotal);

        when(orcamentoRepository.findByOrdemServico_IdAndEmpresa_IdOrderByDataCriacaoDesc(ordemServicoId, EMPRESA_ID)).thenReturn(List.of(orcamento));
    }

    /*
     * Variante usada quando o teste não chega a ler o valor total
     * do orçamento (ex.: falha antes disso, na validação fiscal).
     */
    private void prepararOrcamentoAprovadoSemValor(Long ordemServicoId) {

        when(orcamento.getStatus()).thenReturn(StatusOrcamento.APROVADO);

        when(orcamentoRepository.findByOrdemServico_IdAndEmpresa_IdOrderByDataCriacaoDesc(ordemServicoId, EMPRESA_ID)).thenReturn(List.of(orcamento));
    }

    @Test
    void deveCriarNotaHerdandoCodigoEAliquotaDaEmpresa() {

        construirService();

        prepararEmpresaAtual();

        prepararOrdemServicoValida(1L);

        prepararOrcamentoAprovado(1L, new BigDecimal("1000.00"));

        prepararCadastroFiscalCompleto();

        when(notaFiscalServicoRepository.existsByOrdemServico_IdAndEmpresa_IdAndStatusNot(1L, EMPRESA_ID, StatusNotaFiscalServico.CANCELADA)).thenReturn(false);

        when(empresa.getCodigoServicoPadrao()).thenReturn("01.07");

        when(empresa.getAliquotaIssPadrao()).thenReturn(new BigDecimal("5.00"));

        when(notaFiscalServicoRepository.save(any(NotaFiscalServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(ordemServico.getId()).thenReturn(1L);

        when(orcamento.getId()).thenReturn(10L);

        NotaFiscalServicoRequestDTO dto = new NotaFiscalServicoRequestDTO("Manutenção preventiva de ar-condicionado", null, null);

        NotaFiscalServicoResponseDTO response = service.criar(1L, dto);

        assertEquals(StatusNotaFiscalServico.RASCUNHO, response.status());

        assertEquals("01.07", response.codigoServico());

        assertEquals(new BigDecimal("5.00"), response.aliquotaIss());

        assertEquals(new BigDecimal("50.00"), response.valorIss());

        verify(notaFiscalServicoRepository).save(any(NotaFiscalServico.class));
    }

    @Test
    void deveCriarNotaComCodigoEAliquotaSobrescritosNoRequest() {

        construirService();

        prepararEmpresaAtual();

        prepararOrdemServicoValida(1L);

        prepararOrcamentoAprovado(1L, new BigDecimal("1000.00"));

        prepararCadastroFiscalCompleto();

        when(notaFiscalServicoRepository.existsByOrdemServico_IdAndEmpresa_IdAndStatusNot(1L, EMPRESA_ID, StatusNotaFiscalServico.CANCELADA)).thenReturn(false);

        when(notaFiscalServicoRepository.save(any(NotaFiscalServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(ordemServico.getId()).thenReturn(1L);

        when(orcamento.getId()).thenReturn(10L);

        NotaFiscalServicoRequestDTO dto = new NotaFiscalServicoRequestDTO("Instalação de equipamento", "01.99", new BigDecimal("2.00"));

        NotaFiscalServicoResponseDTO response = service.criar(1L, dto);

        assertEquals("01.99", response.codigoServico());

        assertEquals(new BigDecimal("2.00"), response.aliquotaIss());

        assertEquals(new BigDecimal("20.00"), response.valorIss());
    }

    @Test
    void deveImpedirCriacaoQuandoJaExistirNotaAtivaParaAOrdemServico() {

        construirService();

        prepararEmpresaAtual();

        prepararOrdemServicoEncontrada(1L);

        when(notaFiscalServicoRepository.existsByOrdemServico_IdAndEmpresa_IdAndStatusNot(1L, EMPRESA_ID, StatusNotaFiscalServico.CANCELADA)).thenReturn(true);

        NotaFiscalServicoRequestDTO dto = new NotaFiscalServicoRequestDTO("Descrição", null, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> service.criar(1L, dto));

        assertEquals("Já existe uma nota fiscal ativa para esta ordem de serviço", exception.getMessage());

        verify(notaFiscalServicoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoOrdemServicoNaoExistir() {

        construirService();

        prepararEmpresaAtual();

        when(ordemServicoRepository.findByIdAndEmpresa_Id(999L, EMPRESA_ID)).thenReturn(Optional.empty());

        NotaFiscalServicoRequestDTO dto = new NotaFiscalServicoRequestDTO("Descrição", null, null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.criar(999L, dto));

        assertEquals("Ordem de serviço com ID 999 não encontrada", exception.getMessage());
    }

    @Test
    void deveImpedirCriacaoQuandoNaoHouverOrcamentoAprovado() {

        construirService();

        prepararEmpresaAtual();

        prepararOrdemServicoEncontrada(1L);

        when(notaFiscalServicoRepository.existsByOrdemServico_IdAndEmpresa_IdAndStatusNot(1L, EMPRESA_ID, StatusNotaFiscalServico.CANCELADA)).thenReturn(false);

        when(orcamento.getStatus()).thenReturn(StatusOrcamento.RASCUNHO);

        when(orcamentoRepository.findByOrdemServico_IdAndEmpresa_IdOrderByDataCriacaoDesc(1L, EMPRESA_ID)).thenReturn(List.of(orcamento));

        NotaFiscalServicoRequestDTO dto = new NotaFiscalServicoRequestDTO("Descrição", null, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> service.criar(1L, dto));

        assertEquals("A ordem de serviço não possui orçamento aprovado", exception.getMessage());
    }

    @Test
    void deveImpedirCriacaoQuandoCadastroFiscalIncompleto() {

        construirService();

        prepararEmpresaAtual();

        prepararOrdemServicoValida(1L);

        prepararOrcamentoAprovadoSemValor(1L);

        when(notaFiscalServicoRepository.existsByOrdemServico_IdAndEmpresa_IdAndStatusNot(1L, EMPRESA_ID, StatusNotaFiscalServico.CANCELADA)).thenReturn(false);

        when(empresa.getEndereco()).thenReturn(null);

        when(empresa.getRegimeTributario()).thenReturn(null);

        when(cliente.getEndereco()).thenReturn(null);

        NotaFiscalServicoRequestDTO dto = new NotaFiscalServicoRequestDTO("Descrição", null, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> service.criar(1L, dto));

        assertTrue(exception.getMessage().contains("endereço da empresa"));

        assertTrue(exception.getMessage().contains("regime tributário da empresa"));

        assertTrue(exception.getMessage().contains("endereço do cliente"));

        verify(notaFiscalServicoRepository, never()).save(any());
    }

    private NotaFiscalServico prepararNotaExistente(StatusNotaFiscalServico status) {

        NotaFiscalServico nota = new NotaFiscalServico(ordemServico, orcamento, "Descrição original", "01.07", new BigDecimal("5.00"), new BigDecimal("1000.00"), new BigDecimal("50.00"), AmbienteNotaFiscal.HOMOLOGACAO, empresa);

        if (status != StatusNotaFiscalServico.RASCUNHO) {

            nota.setStatus(status);
        }

        when(notaFiscalServicoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(nota));

        return nota;
    }

    @Test
    void deveAtualizarRascunho() {

        construirService();

        prepararEmpresaAtual();

        NotaFiscalServico nota = prepararNotaExistente(StatusNotaFiscalServico.RASCUNHO);

        when(notaFiscalServicoRepository.save(nota)).thenReturn(nota);

        NotaFiscalServicoRequestDTO dto = new NotaFiscalServicoRequestDTO("Descrição atualizada", "01.99", new BigDecimal("3.00"));

        NotaFiscalServicoResponseDTO response = service.atualizar(1L, dto);

        assertEquals("Descrição atualizada", response.discriminacaoServico());

        assertEquals("01.99", response.codigoServico());

        assertEquals(new BigDecimal("30.00"), response.valorIss());
    }

    @Test
    void deveImpedirAtualizarNotaQueNaoEstaEmRascunho() {

        construirService();

        prepararEmpresaAtual();

        prepararNotaExistente(StatusNotaFiscalServico.ENVIADA);

        NotaFiscalServicoRequestDTO dto = new NotaFiscalServicoRequestDTO("Nova descrição", null, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> service.atualizar(1L, dto));

        assertEquals("Somente notas em rascunho podem ser alteradas", exception.getMessage());

        verify(notaFiscalServicoRepository, never()).save(any());
    }

    @Test
    void deveGerarPayloadParaNotaEmRascunho() {

        construirService();

        prepararEmpresaAtual();

        NotaFiscalServico nota = prepararNotaExistente(StatusNotaFiscalServico.RASCUNHO);

        when(empresa.getNome()).thenReturn("ClimaService Instalações");

        when(empresa.getEndereco()).thenReturn(enderecoCompleto);

        when(ordemServico.getCliente()).thenReturn(cliente);

        when(cliente.getNome()).thenReturn("Cliente Teste");

        when(cliente.getEndereco()).thenReturn(enderecoCompleto);

        when(notaFiscalServicoRepository.save(nota)).thenReturn(nota);

        NotaFiscalServicoResponseDTO response = service.gerarPayload(1L);

        assertNotNull(response.payloadMontado());

        assertTrue(response.payloadMontado().contains("ClimaService Instalações"));

        assertTrue(response.payloadMontado().contains("Cliente Teste"));
    }

    @Test
    void deveImpedirGerarPayloadQuandoNaoEstaEmRascunho() {

        construirService();

        prepararEmpresaAtual();

        prepararNotaExistente(StatusNotaFiscalServico.CANCELADA);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> service.gerarPayload(1L));

        assertEquals("Somente notas em rascunho podem ter o payload gerado", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoDeIndisponibilidadeAoEnviarNotaEmRascunho() {

        construirService();

        prepararEmpresaAtual();

        prepararNotaExistente(StatusNotaFiscalServico.RASCUNHO);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> service.enviar(1L));

        assertEquals("Envio à prefeitura ainda não disponível nesta instalação — requer certificado digital A1/A3 configurado (Fase 2)", exception.getMessage());
    }

    @Test
    void deveImpedirEnviarNotaQueNaoEstaEmRascunho() {

        construirService();

        prepararEmpresaAtual();

        prepararNotaExistente(StatusNotaFiscalServico.AUTORIZADA);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> service.enviar(1L));

        assertEquals("Somente notas em rascunho podem ser enviadas", exception.getMessage());
    }

    @Test
    void deveCancelarRascunho() {

        construirService();

        prepararEmpresaAtual();

        NotaFiscalServico nota = prepararNotaExistente(StatusNotaFiscalServico.RASCUNHO);

        when(notaFiscalServicoRepository.save(nota)).thenReturn(nota);

        NotaFiscalServicoResponseDTO response = service.cancelar(1L);

        assertEquals(StatusNotaFiscalServico.CANCELADA, response.status());
    }

    @Test
    void deveImpedirCancelarNotaQueNaoEstaEmRascunho() {

        construirService();

        prepararEmpresaAtual();

        prepararNotaExistente(StatusNotaFiscalServico.AUTORIZADA);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> service.cancelar(1L));

        assertEquals("Somente notas em rascunho podem ser canceladas nesta fase", exception.getMessage());

        verify(notaFiscalServicoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoNotaNaoExistir() {

        construirService();

        prepararEmpresaAtual();

        when(notaFiscalServicoRepository.findByIdAndEmpresa_Id(999L, EMPRESA_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId(999L));

        assertEquals("Nota fiscal de serviço com ID 999 não encontrada", exception.getMessage());
    }
}
