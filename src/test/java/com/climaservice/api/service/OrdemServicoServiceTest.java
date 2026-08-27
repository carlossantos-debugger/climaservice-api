package com.climaservice.api.service;

import com.climaservice.api.dto.AtualizarDiagnosticoRequestDTO;
import com.climaservice.api.dto.AtualizarStatusOrdemServicoRequestDTO;
import com.climaservice.api.dto.OrdemServicoRequestDTO;
import com.climaservice.api.entity.*;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.ClienteRepository;
import com.climaservice.api.repository.EquipamentoRepository;
import com.climaservice.api.repository.OrdemServicoDiagnosticoHistoricoRepository;
import com.climaservice.api.repository.OrdemServicoHistoricoRepository;
import com.climaservice.api.repository.OrdemServicoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private OrdemServicoHistoricoRepository historicoRepository;

    @Mock
    private OrdemServicoDiagnosticoHistoricoRepository diagnosticoHistoricoRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private Cliente cliente;

    @Mock
    private Equipamento equipamento;

    @Mock
    private Usuario usuario;

    @InjectMocks
    private OrdemServicoService ordemServicoService;


    @Test
    void deveCriarOrdemServicoQuandoDadosForemValidos() {

        // Arrange
        OrdemServicoRequestDTO dto = mock(OrdemServicoRequestDTO.class);

        when(dto.clienteId()).thenReturn(1L);
        when(dto.equipamentoId()).thenReturn(2L);
        when(dto.descricaoProblema()).thenReturn("Ar-condicionado não está resfriando");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        when(equipamentoRepository.findById(2L)).thenReturn(Optional.of(equipamento));

        when(cliente.getId()).thenReturn(1L);
        when(cliente.getNome()).thenReturn("Cliente Teste");

        when(equipamento.getId()).thenReturn(2L);
        when(equipamento.getCliente()).thenReturn(cliente);
        when(equipamento.getStatus()).thenReturn(StatusEquipamento.ATIVO);
        when(equipamento.getMarca()).thenReturn("LG");
        when(equipamento.getModelo()).thenReturn("Dual Inverter");

        when(usuarioAutenticadoService.obterUsuarioAtual()).thenReturn(usuario);

        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = ordemServicoService.salvar(dto);

        // Assert
        assertNotNull(response);

        assertEquals(StatusOrdemServico.ABERTA, response.status());

        assertEquals("Ar-condicionado não está resfriando", response.descricaoProblema());

        ArgumentCaptor<OrdemServico> ordemCaptor = ArgumentCaptor.forClass(OrdemServico.class);

        verify(ordemServicoRepository).save(ordemCaptor.capture());

        OrdemServico ordemSalva = ordemCaptor.getValue();

        assertEquals(StatusOrdemServico.ABERTA, ordemSalva.getStatus());

        assertEquals(cliente, ordemSalva.getCliente());

        assertEquals(equipamento, ordemSalva.getEquipamento());

        verify(historicoRepository).save(any(OrdemServicoHistorico.class));
    }


    @Test
    void deveImpedirCriacaoQuandoEquipamentoNaoPertenceAoCliente() {

        // Arrange
        OrdemServicoRequestDTO dto = mock(OrdemServicoRequestDTO.class);

        Cliente outroCliente = mock(Cliente.class);

        when(dto.clienteId()).thenReturn(1L);
        when(dto.equipamentoId()).thenReturn(2L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        when(equipamentoRepository.findById(2L)).thenReturn(Optional.of(equipamento));

        when(cliente.getId()).thenReturn(1L);

        when(equipamento.getCliente()).thenReturn(outroCliente);

        when(outroCliente.getId()).thenReturn(99L);

        // Act + Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> ordemServicoService.salvar(dto));

        assertEquals("O equipamento informado não pertence ao cliente", exception.getMessage());

        verify(ordemServicoRepository, never()).save(any());

        verify(historicoRepository, never()).save(any());

        verifyNoInteractions(usuarioAutenticadoService);
    }


    @Test
    void deveImpedirCriacaoParaEquipamentoInativo() {

        // Arrange
        OrdemServicoRequestDTO dto = mock(OrdemServicoRequestDTO.class);

        when(dto.clienteId()).thenReturn(1L);
        when(dto.equipamentoId()).thenReturn(2L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        when(equipamentoRepository.findById(2L)).thenReturn(Optional.of(equipamento));

        when(cliente.getId()).thenReturn(1L);

        when(equipamento.getCliente()).thenReturn(cliente);

        when(equipamento.getStatus()).thenReturn(StatusEquipamento.INATIVO);

        // Act + Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> ordemServicoService.salvar(dto));

        assertEquals("Não é possível abrir uma ordem de serviço para um equipamento inativo", exception.getMessage());

        verify(ordemServicoRepository, never()).save(any());

        verify(historicoRepository, never()).save(any());
    }


    @Test
    void deveAlterarStatusDeAbertaParaEmAndamento() {

        OrdemServico ordem = criarOrdem(StatusOrdemServico.ABERTA);

        prepararAtualizacaoStatus(10L, ordem, StatusOrdemServico.EM_ANDAMENTO);

        var response = ordemServicoService.atualizarStatus(10L, dtoStatus(StatusOrdemServico.EM_ANDAMENTO));

        assertEquals(StatusOrdemServico.EM_ANDAMENTO, response.status());

        assertEquals(StatusOrdemServico.EM_ANDAMENTO, ordem.getStatus());

        verify(ordemServicoRepository).save(ordem);

        verificarHistoricoStatus(StatusOrdemServico.ABERTA, StatusOrdemServico.EM_ANDAMENTO);
    }


    @Test
    void deveAlterarStatusDeAbertaParaCancelada() {

        OrdemServico ordem = criarOrdem(StatusOrdemServico.ABERTA);

        prepararAtualizacaoStatus(10L, ordem, StatusOrdemServico.CANCELADA);

        ordemServicoService.atualizarStatus(10L, dtoStatus(StatusOrdemServico.CANCELADA));

        assertEquals(StatusOrdemServico.CANCELADA, ordem.getStatus());

        verificarHistoricoStatus(StatusOrdemServico.ABERTA, StatusOrdemServico.CANCELADA);
    }


    @Test
    void deveImpedirTransicaoDeAbertaParaConcluida() {

        OrdemServico ordem = criarOrdem(StatusOrdemServico.ABERTA);

        when(ordemServicoRepository.findById(10L)).thenReturn(Optional.of(ordem));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> ordemServicoService.atualizarStatus(10L, dtoStatus(StatusOrdemServico.CONCLUIDA)));

        assertEquals("Transição de status inválida: ABERTA -> CONCLUIDA", exception.getMessage());

        assertEquals(StatusOrdemServico.ABERTA, ordem.getStatus());

        verify(ordemServicoRepository, never()).save(any());

        verify(historicoRepository, never()).save(any());

        verifyNoInteractions(usuarioAutenticadoService);
    }


    @Test
    void deveAlterarStatusDeEmAndamentoParaAguardandoCliente() {

        OrdemServico ordem = criarOrdem(StatusOrdemServico.EM_ANDAMENTO);

        prepararAtualizacaoStatus(10L, ordem, StatusOrdemServico.AGUARDANDO_CLIENTE);

        ordemServicoService.atualizarStatus(10L, dtoStatus(StatusOrdemServico.AGUARDANDO_CLIENTE));

        assertEquals(StatusOrdemServico.AGUARDANDO_CLIENTE, ordem.getStatus());

        verificarHistoricoStatus(StatusOrdemServico.EM_ANDAMENTO, StatusOrdemServico.AGUARDANDO_CLIENTE);
    }


    @Test
    void deveConcluirOrdemEmAndamentoEPreencherDataConclusao() {

        OrdemServico ordem = criarOrdem(StatusOrdemServico.EM_ANDAMENTO);

        prepararAtualizacaoStatus(10L, ordem, StatusOrdemServico.CONCLUIDA);

        var response = ordemServicoService.atualizarStatus(10L, dtoStatus(StatusOrdemServico.CONCLUIDA));

        assertEquals(StatusOrdemServico.CONCLUIDA, ordem.getStatus());

        assertNotNull(ordem.getDataConclusao());

        assertNotNull(response.dataConclusao());

        verificarHistoricoStatus(StatusOrdemServico.EM_ANDAMENTO, StatusOrdemServico.CONCLUIDA);
    }


    @Test
    void deveAlterarStatusDeAguardandoClienteParaEmAndamento() {

        OrdemServico ordem = criarOrdem(StatusOrdemServico.AGUARDANDO_CLIENTE);

        prepararAtualizacaoStatus(10L, ordem, StatusOrdemServico.EM_ANDAMENTO);

        ordemServicoService.atualizarStatus(10L, dtoStatus(StatusOrdemServico.EM_ANDAMENTO));

        assertEquals(StatusOrdemServico.EM_ANDAMENTO, ordem.getStatus());

        verificarHistoricoStatus(StatusOrdemServico.AGUARDANDO_CLIENTE, StatusOrdemServico.EM_ANDAMENTO);
    }


    @Test
    void deveImpedirAlteracaoDeOrdemConcluida() {

        OrdemServico ordem = criarOrdem(StatusOrdemServico.CONCLUIDA);

        when(ordemServicoRepository.findById(10L)).thenReturn(Optional.of(ordem));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> ordemServicoService.atualizarStatus(10L, dtoStatus(StatusOrdemServico.CANCELADA)));

        assertEquals("Transição de status inválida: CONCLUIDA -> CANCELADA", exception.getMessage());

        verify(ordemServicoRepository, never()).save(any());

        verify(historicoRepository, never()).save(any());
    }


    @Test
    void deveImpedirAlteracaoDeOrdemCancelada() {

        OrdemServico ordem = criarOrdem(StatusOrdemServico.CANCELADA);

        when(ordemServicoRepository.findById(10L)).thenReturn(Optional.of(ordem));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> ordemServicoService.atualizarStatus(10L, dtoStatus(StatusOrdemServico.EM_ANDAMENTO)));

        assertEquals("Transição de status inválida: CANCELADA -> EM_ANDAMENTO", exception.getMessage());

        verify(ordemServicoRepository, never()).save(any());

        verify(historicoRepository, never()).save(any());
    }


    @Test
    void deveAtualizarDiagnostico() {

        // Arrange
        OrdemServico ordem = criarOrdem(StatusOrdemServico.EM_ANDAMENTO);

        ordem.setDiagnostico("Diagnóstico anterior");

        AtualizarDiagnosticoRequestDTO dto = mock(AtualizarDiagnosticoRequestDTO.class);

        when(dto.diagnostico()).thenReturn("Compressor com defeito");

        when(ordemServicoRepository.findById(10L)).thenReturn(Optional.of(ordem));

        when(ordemServicoRepository.save(ordem)).thenReturn(ordem);

        when(usuarioAutenticadoService.obterUsuarioAtual()).thenReturn(usuario);

        // Act
        var response = ordemServicoService.atualizarDiagnostico(10L, dto);

        // Assert
        assertEquals("Compressor com defeito", ordem.getDiagnostico());

        assertEquals("Compressor com defeito", response.diagnostico());

        ArgumentCaptor<OrdemServicoDiagnosticoHistorico> captor = ArgumentCaptor.forClass(OrdemServicoDiagnosticoHistorico.class);

        verify(diagnosticoHistoricoRepository).save(captor.capture());

        OrdemServicoDiagnosticoHistorico historico = captor.getValue();

        assertEquals("Diagnóstico anterior", historico.getDiagnosticoAnterior());

        assertEquals("Compressor com defeito", historico.getDiagnosticoNovo());

        assertEquals(usuario, historico.getUsuario());
    }


    @Test
    void deveImpedirDiagnosticoEmOrdemConcluida() {

        OrdemServico ordem = criarOrdem(StatusOrdemServico.CONCLUIDA);

        when(ordemServicoRepository.findById(10L)).thenReturn(Optional.of(ordem));

        AtualizarDiagnosticoRequestDTO dto = mock(AtualizarDiagnosticoRequestDTO.class);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> ordemServicoService.atualizarDiagnostico(10L, dto));

        assertEquals("Não é possível alterar o diagnóstico de uma ordem concluída ou cancelada", exception.getMessage());

        verify(ordemServicoRepository, never()).save(any());

        verify(diagnosticoHistoricoRepository, never()).save(any());
    }


    @Test
    void deveImpedirDiagnosticoEmOrdemCancelada() {

        OrdemServico ordem = criarOrdem(StatusOrdemServico.CANCELADA);

        when(ordemServicoRepository.findById(10L)).thenReturn(Optional.of(ordem));

        AtualizarDiagnosticoRequestDTO dto = mock(AtualizarDiagnosticoRequestDTO.class);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> ordemServicoService.atualizarDiagnostico(10L, dto));

        assertEquals("Não é possível alterar o diagnóstico de uma ordem concluída ou cancelada", exception.getMessage());

        verify(ordemServicoRepository, never()).save(any());

        verify(diagnosticoHistoricoRepository, never()).save(any());
    }


    @Test
    void deveRetornarOptionalVazioQuandoOrdemServicoNaoExistir() {

        // Arrange
        when(ordemServicoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        var resultado = ordemServicoService.buscarPorId(999L);

        // Assert
        assertTrue(resultado.isEmpty());

        verify(ordemServicoRepository).findById(999L);
    }


    /*
     * Helpers
     */

    private OrdemServico criarOrdem(StatusOrdemServico status) {

        OrdemServico ordem = new OrdemServico(cliente, equipamento, "Problema de teste");

        ordem.setStatus(status);

        return ordem;
    }


    private AtualizarStatusOrdemServicoRequestDTO dtoStatus(StatusOrdemServico status) {

        AtualizarStatusOrdemServicoRequestDTO dto = mock(AtualizarStatusOrdemServicoRequestDTO.class);

        when(dto.status()).thenReturn(status);

        return dto;
    }


    private void prepararAtualizacaoStatus(Long id, OrdemServico ordem, StatusOrdemServico novoStatus) {

        when(ordemServicoRepository.findById(id)).thenReturn(Optional.of(ordem));

        when(ordemServicoRepository.save(ordem)).thenReturn(ordem);

        when(usuarioAutenticadoService.obterUsuarioAtual()).thenReturn(usuario);
    }


    private void verificarHistoricoStatus(StatusOrdemServico statusAnterior, StatusOrdemServico statusNovo) {

        ArgumentCaptor<OrdemServicoHistorico> captor = ArgumentCaptor.forClass(OrdemServicoHistorico.class);

        verify(historicoRepository).save(captor.capture());

        OrdemServicoHistorico historico = captor.getValue();

        assertEquals(statusAnterior, historico.getStatusAnterior());

        assertEquals(statusNovo, historico.getStatusNovo());

        assertEquals(usuario, historico.getUsuario());
    }

    @Test
    void deveLancarExcecaoAoAtualizarStatusDeOrdemInexistente() {

        // Arrange
        Long ordemId = 999L;

        when(ordemServicoRepository.findById(ordemId)).thenReturn(Optional.empty());

        AtualizarStatusOrdemServicoRequestDTO dto = mock(AtualizarStatusOrdemServicoRequestDTO.class);

        // Act + Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> ordemServicoService.atualizarStatus(ordemId, dto));

        assertEquals("Ordem de serviço com ID 999 não encontrada", exception.getMessage());

        verify(ordemServicoRepository, never()).save(any());

        verify(historicoRepository, never()).save(any());
    }
}