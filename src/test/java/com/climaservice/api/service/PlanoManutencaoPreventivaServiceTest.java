package com.climaservice.api.service;

import com.climaservice.api.dto.OrdemServicoResponseDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaAtualizarRequestDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaExecucaoResponseDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaRequestDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaResponseDTO;
import com.climaservice.api.entity.*;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.EquipamentoRepository;
import com.climaservice.api.repository.OrdemServicoRepository;
import com.climaservice.api.repository.PlanoManutencaoPreventivaExecucaoRepository;
import com.climaservice.api.repository.PlanoManutencaoPreventivaRepository;
import com.climaservice.api.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanoManutencaoPreventivaServiceTest {

    private static final Long EMPRESA_ID = 8001L;

    @Mock
    private PlanoManutencaoPreventivaRepository planoRepository;

    @Mock
    private PlanoManutencaoPreventivaExecucaoRepository execucaoRepository;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private OrdemServicoService ordemServicoService;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private Empresa empresa;

    @Mock
    private Equipamento equipamento;

    @Mock
    private Cliente cliente;

    @Mock
    private Usuario tecnico;

    @Mock
    private Usuario usuarioAtual;

    @Mock
    private OrdemServico ordemServico;

    @InjectMocks
    private PlanoManutencaoPreventivaService planoService;

    private final LocalDate proximaExecucao = LocalDate.now();

    private void prepararEmpresaAtual() {

        when(usuarioAutenticadoService.obterEmpresaAtual()).thenReturn(empresa);

        when(empresa.getId()).thenReturn(EMPRESA_ID);
    }

    private void prepararEquipamentoValido(Long equipamentoId) {

        prepararEmpresaAtual();

        when(equipamentoRepository.findByIdAndEmpresa_Id(equipamentoId, EMPRESA_ID)).thenReturn(Optional.of(equipamento));

        when(equipamento.getStatus()).thenReturn(StatusEquipamento.ATIVO);
    }

    private void prepararTecnicoValido(Long tecnicoId) {

        when(usuarioRepository.findByIdAndEmpresa_Id(tecnicoId, EMPRESA_ID)).thenReturn(Optional.of(tecnico));

        when(tecnico.getRole()).thenReturn(RoleUsuario.TECNICO);

        when(tecnico.getAtivo()).thenReturn(true);
    }

    @Test
    void deveCriarPlanoQuandoDadosForemValidos() {

        prepararEquipamentoValido(1L);

        when(equipamento.getId()).thenReturn(1L);

        when(equipamento.getMarca()).thenReturn("Marca");

        when(equipamento.getModelo()).thenReturn("Modelo");

        when(planoRepository.save(any(PlanoManutencaoPreventiva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanoManutencaoPreventivaRequestDTO dto = new PlanoManutencaoPreventivaRequestDTO(1L, null, 6, proximaExecucao, "Observação");

        PlanoManutencaoPreventivaResponseDTO response = planoService.criar(dto);

        assertNotNull(response);

        assertEquals(1L, response.equipamentoId());

        assertEquals(proximaExecucao, response.proximaExecucao());

        assertTrue(response.ativo());

        verify(planoRepository).save(any(PlanoManutencaoPreventiva.class));

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveCalcularProximaExecucaoPadraoQuandoNaoInformada() {

        prepararEquipamentoValido(1L);

        when(equipamento.getId()).thenReturn(1L);

        when(equipamento.getMarca()).thenReturn("Marca");

        when(equipamento.getModelo()).thenReturn("Modelo");

        when(planoRepository.save(any(PlanoManutencaoPreventiva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanoManutencaoPreventivaRequestDTO dto = new PlanoManutencaoPreventivaRequestDTO(1L, null, 6, null, null);

        PlanoManutencaoPreventivaResponseDTO response = planoService.criar(dto);

        assertEquals(LocalDate.now().plusMonths(6), response.proximaExecucao());
    }

    @Test
    void deveImpedirCriacaoParaEquipamentoInativo() {

        prepararEquipamentoValido(1L);

        when(equipamento.getStatus()).thenReturn(StatusEquipamento.INATIVO);

        PlanoManutencaoPreventivaRequestDTO dto = new PlanoManutencaoPreventivaRequestDTO(1L, null, 6, proximaExecucao, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> planoService.criar(dto));

        assertEquals("Não é possível criar um plano de manutenção preventiva para um equipamento inativo", exception.getMessage());

        verify(planoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoEquipamentoNaoExistir() {

        prepararEmpresaAtual();

        when(equipamentoRepository.findByIdAndEmpresa_Id(999L, EMPRESA_ID)).thenReturn(Optional.empty());

        PlanoManutencaoPreventivaRequestDTO dto = new PlanoManutencaoPreventivaRequestDTO(999L, null, 6, proximaExecucao, null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> planoService.criar(dto));

        assertEquals("Equipamento com ID 999 não encontrado", exception.getMessage());
    }

    @Test
    void deveImpedirCriacaoComTecnicoQueNaoETecnico() {

        prepararEquipamentoValido(1L);

        when(usuarioRepository.findByIdAndEmpresa_Id(2L, EMPRESA_ID)).thenReturn(Optional.of(tecnico));

        when(tecnico.getRole()).thenReturn(RoleUsuario.ATENDENTE);

        PlanoManutencaoPreventivaRequestDTO dto = new PlanoManutencaoPreventivaRequestDTO(1L, 2L, 6, proximaExecucao, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> planoService.criar(dto));

        assertEquals("O usuário informado não possui perfil de técnico", exception.getMessage());

        verify(planoRepository, never()).save(any());
    }

    @Test
    void deveImpedirCriacaoComTecnicoInativo() {

        prepararEquipamentoValido(1L);

        when(usuarioRepository.findByIdAndEmpresa_Id(2L, EMPRESA_ID)).thenReturn(Optional.of(tecnico));

        when(tecnico.getRole()).thenReturn(RoleUsuario.TECNICO);

        when(tecnico.getAtivo()).thenReturn(false);

        PlanoManutencaoPreventivaRequestDTO dto = new PlanoManutencaoPreventivaRequestDTO(1L, 2L, 6, proximaExecucao, null);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> planoService.criar(dto));

        assertEquals("O técnico informado está inativo", exception.getMessage());

        verify(planoRepository, never()).save(any());
    }

    @Test
    void deveAtivarPlano() {

        prepararEmpresaAtual();

        PlanoManutencaoPreventiva plano = mock(PlanoManutencaoPreventiva.class);

        when(planoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(plano));

        when(plano.getEquipamento()).thenReturn(equipamento);

        when(planoRepository.save(plano)).thenReturn(plano);

        planoService.ativar(1L);

        verify(plano).setAtivo(true);
    }

    @Test
    void deveInativarPlano() {

        prepararEmpresaAtual();

        PlanoManutencaoPreventiva plano = mock(PlanoManutencaoPreventiva.class);

        when(planoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(plano));

        when(plano.getEquipamento()).thenReturn(equipamento);

        when(planoRepository.save(plano)).thenReturn(plano);

        planoService.inativar(1L);

        verify(plano).setAtivo(false);
    }

    @Test
    void deveLancarExcecaoQuandoPlanoNaoExistir() {

        prepararEmpresaAtual();

        when(planoRepository.findByIdAndEmpresa_Id(999L, EMPRESA_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> planoService.buscarPorId(999L));

        assertEquals("Plano de manutenção preventiva com ID 999 não encontrado", exception.getMessage());
    }

    @Test
    void deveAtualizarPlano() {

        prepararEmpresaAtual();

        PlanoManutencaoPreventiva plano = mock(PlanoManutencaoPreventiva.class);

        when(planoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(plano));

        when(plano.getEquipamento()).thenReturn(equipamento);

        when(planoRepository.save(plano)).thenReturn(plano);

        LocalDate novaProximaExecucao = proximaExecucao.plusMonths(1);

        PlanoManutencaoPreventivaAtualizarRequestDTO dto = new PlanoManutencaoPreventivaAtualizarRequestDTO(null, 12, novaProximaExecucao, "Nova observação");

        planoService.atualizar(1L, dto);

        verify(plano).setIntervaloMeses(12);

        verify(plano).setProximaExecucao(novaProximaExecucao);

        verify(plano).setObservacao("Nova observação");

        verify(plano).setTecnicoPadrao(null);
    }

    private PlanoManutencaoPreventiva prepararPlanoElegivelParaGeracao() {

        prepararEmpresaAtual();

        PlanoManutencaoPreventiva plano = mock(PlanoManutencaoPreventiva.class);

        when(planoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(plano));

        when(plano.getId()).thenReturn(1L);

        when(plano.getAtivo()).thenReturn(true);

        when(plano.getEquipamento()).thenReturn(equipamento);

        when(plano.getIntervaloMeses()).thenReturn(6);

        when(plano.getProximaExecucao()).thenReturn(proximaExecucao);

        when(equipamento.getStatus()).thenReturn(StatusEquipamento.ATIVO);

        when(equipamento.getCliente()).thenReturn(cliente);

        when(equipamento.getId()).thenReturn(1L);

        when(cliente.getId()).thenReturn(5L);

        return plano;
    }

    @Test
    void deveGerarOrdemServicoQuandoPlanoElegivel() {

        PlanoManutencaoPreventiva plano = prepararPlanoElegivelParaGeracao();

        when(execucaoRepository.existsByPlano_IdAndDataReferencia(1L, proximaExecucao)).thenReturn(false);

        OrdemServicoResponseDTO ordemServicoResponse = new OrdemServicoResponseDTO(10L, 5L, "Cliente", 1L, "Marca", "Modelo", "Manutenção preventiva programada", null, StatusOrdemServico.ABERTA, LocalDateTime.now(), null);

        when(ordemServicoService.salvar(any())).thenReturn(ordemServicoResponse);

        when(ordemServicoRepository.findByIdAndEmpresa_Id(10L, EMPRESA_ID)).thenReturn(Optional.of(ordemServico));

        when(ordemServico.getId()).thenReturn(10L);

        when(usuarioAutenticadoService.obterUsuarioAtual()).thenReturn(usuarioAtual);

        when(execucaoRepository.save(any(PlanoManutencaoPreventivaExecucao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanoManutencaoPreventivaExecucaoResponseDTO response = planoService.gerarOrdemServico(1L);

        assertNotNull(response);

        assertEquals(10L, response.ordemServicoId());

        verify(plano).setUltimaExecucao(proximaExecucao);

        verify(plano).setProximaExecucao(proximaExecucao.plusMonths(6));

        verify(planoRepository).save(plano);

        verify(execucaoRepository).save(any(PlanoManutencaoPreventivaExecucao.class));
    }

    @Test
    void deveImpedirGeracaoQuandoPlanoInativo() {

        prepararEmpresaAtual();

        PlanoManutencaoPreventiva plano = mock(PlanoManutencaoPreventiva.class);

        when(planoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(plano));

        when(plano.getAtivo()).thenReturn(false);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> planoService.gerarOrdemServico(1L));

        assertEquals("O plano de manutenção preventiva está inativo", exception.getMessage());

        verifyNoInteractions(ordemServicoService);
    }

    @Test
    void deveImpedirGeracaoQuandoEquipamentoInativo() {

        prepararEmpresaAtual();

        PlanoManutencaoPreventiva plano = mock(PlanoManutencaoPreventiva.class);

        when(planoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(plano));

        when(plano.getAtivo()).thenReturn(true);

        when(plano.getEquipamento()).thenReturn(equipamento);

        when(equipamento.getStatus()).thenReturn(StatusEquipamento.INATIVO);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> planoService.gerarOrdemServico(1L));

        assertEquals("Não é possível gerar ordem de serviço para um equipamento inativo", exception.getMessage());

        verifyNoInteractions(ordemServicoService);
    }

    @Test
    void deveImpedirGeracaoForaDoPrazo() {

        prepararEmpresaAtual();

        PlanoManutencaoPreventiva plano = mock(PlanoManutencaoPreventiva.class);

        when(planoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(plano));

        when(plano.getAtivo()).thenReturn(true);

        when(plano.getEquipamento()).thenReturn(equipamento);

        when(equipamento.getStatus()).thenReturn(StatusEquipamento.ATIVO);

        when(plano.getProximaExecucao()).thenReturn(LocalDate.now().plusDays(10));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> planoService.gerarOrdemServico(1L));

        assertEquals("A manutenção preventiva ainda não está no prazo de execução", exception.getMessage());

        verifyNoInteractions(ordemServicoService);
    }

    @Test
    void deveImpedirGeracaoDuplicadaParaMesmaOcorrencia() {

        prepararEmpresaAtual();

        PlanoManutencaoPreventiva plano = mock(PlanoManutencaoPreventiva.class);

        when(planoRepository.findByIdAndEmpresa_Id(1L, EMPRESA_ID)).thenReturn(Optional.of(plano));

        when(plano.getId()).thenReturn(1L);

        when(plano.getAtivo()).thenReturn(true);

        when(plano.getEquipamento()).thenReturn(equipamento);

        when(equipamento.getStatus()).thenReturn(StatusEquipamento.ATIVO);

        when(plano.getProximaExecucao()).thenReturn(proximaExecucao);

        when(execucaoRepository.existsByPlano_IdAndDataReferencia(1L, proximaExecucao)).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> planoService.gerarOrdemServico(1L));

        assertEquals("Já existe uma ordem de serviço gerada para esta ocorrência de manutenção preventiva", exception.getMessage());

        verifyNoInteractions(ordemServicoService);
    }
}
