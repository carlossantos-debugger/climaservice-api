package com.climaservice.api.service;

import com.climaservice.api.dto.PagamentoHistoricoResponseDTO;
import com.climaservice.api.dto.PagamentoRequestDTO;
import com.climaservice.api.dto.PagamentoResponseDTO;
import com.climaservice.api.dto.PagamentoResumoResponseDTO;
import com.climaservice.api.entity.*;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.OrcamentoRepository;
import com.climaservice.api.repository.PagamentoHistoricoRepository;
import com.climaservice.api.repository.PagamentoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
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
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private OrcamentoRepository orcamentoRepository;

    @Mock
    private PagamentoHistoricoRepository historicoRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private Orcamento orcamento;

    @Mock
    private Usuario usuario;

    @InjectMocks
    private PagamentoService pagamentoService;


    @Test
    void deveCriarPagamentoPendenteQuandoValorForValido() {

        Long orcamentoId = 10L;

        when(orcamentoRepository.findById(orcamentoId))
                .thenReturn(Optional.of(orcamento));

        when(orcamento.getId())
                .thenReturn(orcamentoId);

        when(orcamento.getStatus())
                .thenReturn(StatusOrcamento.APROVADO);

        when(orcamento.getValorTotal())
                .thenReturn(new BigDecimal("1000.00"));

        when(pagamentoRepository.findByOrcamentoIdAndStatus(
                orcamentoId,
                StatusPagamento.CONFIRMADO
        )).thenReturn(List.of());

        when(pagamentoRepository.findByOrcamentoIdAndStatus(
                orcamentoId,
                StatusPagamento.PENDENTE
        )).thenReturn(List.of());

        when(pagamentoRepository.save(any(Pagamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(usuarioAutenticadoService.obterUsuarioAtual())
                .thenReturn(usuario);

        PagamentoRequestDTO dto = new PagamentoRequestDTO(
                new BigDecimal("250.00"),
                FormaPagamento.PIX,
                "Pagamento válido"
        );

        PagamentoResponseDTO response =
                pagamentoService.criar(orcamentoId, dto);

        assertNotNull(response);
        assertEquals(StatusPagamento.PENDENTE, response.status());
        assertEquals(new BigDecimal("250.00"), response.valor());
        assertEquals(orcamentoId, response.orcamentoId());

        ArgumentCaptor<Pagamento> captor =
                ArgumentCaptor.forClass(Pagamento.class);

        verify(pagamentoRepository).save(captor.capture());

        Pagamento pagamentoSalvo = captor.getValue();

        assertEquals(StatusPagamento.PENDENTE, pagamentoSalvo.getStatus());
        assertEquals(new BigDecimal("250.00"), pagamentoSalvo.getValor());

        verify(historicoRepository)
                .save(any(PagamentoHistorico.class));
    }


    @Test
    void deveImpedirCriacaoQuandoOrcamentoNaoEstaAprovado() {

        Long orcamentoId = 10L;

        when(orcamentoRepository.findById(orcamentoId))
                .thenReturn(Optional.of(orcamento));

        when(orcamento.getStatus())
                .thenReturn(StatusOrcamento.ENVIADO);

        PagamentoRequestDTO dto = new PagamentoRequestDTO(
                new BigDecimal("100.00"),
                FormaPagamento.PIX,
                "Teste"
        );

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> pagamentoService.criar(orcamentoId, dto)
        );

        assertEquals(
                "Somente orçamentos aprovados podem receber pagamentos",
                exception.getMessage()
        );

        verify(pagamentoRepository, never())
                .save(any(Pagamento.class));

        verify(historicoRepository, never())
                .save(any(PagamentoHistorico.class));

        verifyNoInteractions(usuarioAutenticadoService);
    }


    @Test
    void deveImpedirPagamentoAcimaDoSaldoDisponivel() {

        Long orcamentoId = 10L;

        Pagamento pagamentoConfirmado = mock(Pagamento.class);
        Pagamento pagamentoPendente = mock(Pagamento.class);

        when(orcamentoRepository.findById(orcamentoId))
                .thenReturn(Optional.of(orcamento));

        when(orcamento.getId())
                .thenReturn(orcamentoId);

        when(orcamento.getStatus())
                .thenReturn(StatusOrcamento.APROVADO);

        when(orcamento.getValorTotal())
                .thenReturn(new BigDecimal("1000.00"));

        when(pagamentoConfirmado.getValor())
                .thenReturn(new BigDecimal("600.00"));

        when(pagamentoPendente.getValor())
                .thenReturn(new BigDecimal("300.00"));

        when(pagamentoRepository.findByOrcamentoIdAndStatus(
                orcamentoId,
                StatusPagamento.CONFIRMADO
        )).thenReturn(List.of(pagamentoConfirmado));

        when(pagamentoRepository.findByOrcamentoIdAndStatus(
                orcamentoId,
                StatusPagamento.PENDENTE
        )).thenReturn(List.of(pagamentoPendente));

        PagamentoRequestDTO dto = new PagamentoRequestDTO(
                new BigDecimal("150.00"),
                FormaPagamento.PIX,
                "Pagamento acima do saldo"
        );

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> pagamentoService.criar(orcamentoId, dto)
        );

        assertEquals(
                "O valor do pagamento ultrapassa o saldo disponível de 100.00",
                exception.getMessage()
        );

        verify(pagamentoRepository, never())
                .save(any(Pagamento.class));

        verify(historicoRepository, never())
                .save(any(PagamentoHistorico.class));

        verifyNoInteractions(usuarioAutenticadoService);
    }


    @Test
    void devePermitirPagamentoIgualAoSaldoDisponivel() {

        Long orcamentoId = 10L;

        Pagamento pagamentoConfirmado = mock(Pagamento.class);
        Pagamento pagamentoPendente = mock(Pagamento.class);

        when(orcamentoRepository.findById(orcamentoId))
                .thenReturn(Optional.of(orcamento));

        when(orcamento.getId())
                .thenReturn(orcamentoId);

        when(orcamento.getStatus())
                .thenReturn(StatusOrcamento.APROVADO);

        when(orcamento.getValorTotal())
                .thenReturn(new BigDecimal("1000.00"));

        when(pagamentoConfirmado.getValor())
                .thenReturn(new BigDecimal("600.00"));

        when(pagamentoPendente.getValor())
                .thenReturn(new BigDecimal("300.00"));

        when(pagamentoRepository.findByOrcamentoIdAndStatus(
                orcamentoId,
                StatusPagamento.CONFIRMADO
        )).thenReturn(List.of(pagamentoConfirmado));

        when(pagamentoRepository.findByOrcamentoIdAndStatus(
                orcamentoId,
                StatusPagamento.PENDENTE
        )).thenReturn(List.of(pagamentoPendente));

        when(pagamentoRepository.save(any(Pagamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(usuarioAutenticadoService.obterUsuarioAtual())
                .thenReturn(usuario);

        PagamentoRequestDTO dto = new PagamentoRequestDTO(
                new BigDecimal("100.00"),
                FormaPagamento.PIX,
                "Saldo exato"
        );

        PagamentoResponseDTO response =
                pagamentoService.criar(orcamentoId, dto);

        assertNotNull(response);
        assertEquals(StatusPagamento.PENDENTE, response.status());

        verify(pagamentoRepository)
                .save(any(Pagamento.class));

        verify(historicoRepository)
                .save(any(PagamentoHistorico.class));
    }


    @Test
    void deveLancarExcecaoQuandoOrcamentoNaoExistir() {

        Long orcamentoId = 999L;

        when(orcamentoRepository.findById(orcamentoId))
                .thenReturn(Optional.empty());

        PagamentoRequestDTO dto = new PagamentoRequestDTO(
                new BigDecimal("100.00"),
                FormaPagamento.PIX,
                "Teste"
        );

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pagamentoService.criar(orcamentoId, dto)
        );

        assertEquals(
                "Orçamento com ID 999 não encontrado",
                exception.getMessage()
        );

        verifyNoInteractions(pagamentoRepository);
        verifyNoInteractions(historicoRepository);
        verifyNoInteractions(usuarioAutenticadoService);
    }


    @Test
    void deveConfirmarPagamentoPendente() {

        Long pagamentoId = 1L;

        when(orcamento.getId())
                .thenReturn(10L);

        Pagamento pagamento = new Pagamento(
                orcamento,
                new BigDecimal("100.00"),
                FormaPagamento.PIX,
                "Pagamento"
        );

        when(pagamentoRepository.findById(pagamentoId))
                .thenReturn(Optional.of(pagamento));

        when(pagamentoRepository.save(any(Pagamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(usuarioAutenticadoService.obterUsuarioAtual())
                .thenReturn(usuario);

        PagamentoResponseDTO response =
                pagamentoService.confirmar(pagamentoId);

        assertNotNull(response);
        assertEquals(StatusPagamento.CONFIRMADO, response.status());
        assertEquals(StatusPagamento.CONFIRMADO, pagamento.getStatus());

        assertNotNull(pagamento.getDataConfirmacao());

        verify(pagamentoRepository).findById(pagamentoId);
        verify(pagamentoRepository).save(pagamento);

        verify(historicoRepository)
                .save(any(PagamentoHistorico.class));
    }


    @Test
    void deveImpedirConfirmacaoDePagamentoQueNaoEstaPendente() {

        Long pagamentoId = 1L;

        Pagamento pagamento = mock(Pagamento.class);

        when(pagamento.getStatus())
                .thenReturn(StatusPagamento.CONFIRMADO);

        when(pagamentoRepository.findById(pagamentoId))
                .thenReturn(Optional.of(pagamento));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> pagamentoService.confirmar(pagamentoId)
        );

        assertEquals(
                "Somente pagamentos pendentes podem ser alterados",
                exception.getMessage()
        );

        verify(pagamentoRepository).findById(pagamentoId);

        verify(pagamentoRepository, never())
                .save(any(Pagamento.class));

        verify(historicoRepository, never())
                .save(any(PagamentoHistorico.class));

        verifyNoInteractions(usuarioAutenticadoService);
    }


    @Test
    void deveCancelarPagamentoPendente() {

        Long pagamentoId = 1L;

        when(orcamento.getId())
                .thenReturn(10L);

        Pagamento pagamento = new Pagamento(
                orcamento,
                new BigDecimal("100.00"),
                FormaPagamento.PIX,
                "Pagamento"
        );

        when(pagamentoRepository.findById(pagamentoId))
                .thenReturn(Optional.of(pagamento));

        when(pagamentoRepository.save(any(Pagamento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(usuarioAutenticadoService.obterUsuarioAtual())
                .thenReturn(usuario);

        PagamentoResponseDTO response =
                pagamentoService.cancelar(pagamentoId);

        assertNotNull(response);
        assertEquals(StatusPagamento.CANCELADO, response.status());
        assertEquals(StatusPagamento.CANCELADO, pagamento.getStatus());

        assertNotNull(pagamento.getDataCancelamento());

        verify(pagamentoRepository).save(pagamento);

        verify(historicoRepository)
                .save(any(PagamentoHistorico.class));
    }


    @Test
    void deveImpedirCancelamentoDePagamentoQueNaoEstaPendente() {

        Long pagamentoId = 1L;

        Pagamento pagamento = mock(Pagamento.class);

        when(pagamento.getStatus())
                .thenReturn(StatusPagamento.CANCELADO);

        when(pagamentoRepository.findById(pagamentoId))
                .thenReturn(Optional.of(pagamento));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> pagamentoService.cancelar(pagamentoId)
        );

        assertEquals(
                "Somente pagamentos pendentes podem ser alterados",
                exception.getMessage()
        );

        verify(pagamentoRepository, never())
                .save(any(Pagamento.class));

        verify(historicoRepository, never())
                .save(any(PagamentoHistorico.class));

        verifyNoInteractions(usuarioAutenticadoService);
    }


    @Test
    void deveBuscarPagamentoPorId() {

        Long pagamentoId = 1L;

        when(orcamento.getId())
                .thenReturn(10L);

        Pagamento pagamento = new Pagamento(
                orcamento,
                new BigDecimal("150.00"),
                FormaPagamento.PIX,
                "Pagamento"
        );

        when(pagamentoRepository.findById(pagamentoId))
                .thenReturn(Optional.of(pagamento));

        PagamentoResponseDTO response =
                pagamentoService.buscarPorId(pagamentoId);

        assertNotNull(response);
        assertEquals(new BigDecimal("150.00"), response.valor());
        assertEquals(StatusPagamento.PENDENTE, response.status());
        assertEquals(10L, response.orcamentoId());

        verify(pagamentoRepository).findById(pagamentoId);
    }


    @Test
    void deveLancarExcecaoQuandoPagamentoNaoExistir() {

        Long pagamentoId = 999L;

        when(pagamentoRepository.findById(pagamentoId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pagamentoService.buscarPorId(pagamentoId)
        );

        assertEquals(
                "Pagamento com ID 999 não encontrado",
                exception.getMessage()
        );
    }


    @Test
    void deveListarPagamentosPorOrcamento() {

        Long orcamentoId = 10L;

        when(orcamentoRepository.findById(orcamentoId))
                .thenReturn(Optional.of(orcamento));

        when(orcamento.getId())
                .thenReturn(orcamentoId);

        Pagamento pagamento1 = new Pagamento(
                orcamento,
                new BigDecimal("100.00"),
                FormaPagamento.PIX,
                "Pagamento 1"
        );

        Pagamento pagamento2 = new Pagamento(
                orcamento,
                new BigDecimal("200.00"),
                FormaPagamento.DINHEIRO,
                "Pagamento 2"
        );

        when(pagamentoRepository
                .findByOrcamentoIdOrderByDataCriacaoAsc(orcamentoId))
                .thenReturn(List.of(pagamento1, pagamento2));

        List<PagamentoResponseDTO> pagamentos =
                pagamentoService.listarPorOrcamento(orcamentoId);

        assertEquals(2, pagamentos.size());

        assertEquals(
                new BigDecimal("100.00"),
                pagamentos.get(0).valor()
        );

        assertEquals(
                new BigDecimal("200.00"),
                pagamentos.get(1).valor()
        );
    }


    @Test
    void deveCalcularResumoDoOrcamento() {

        Long orcamentoId = 10L;

        Pagamento confirmado1 = mock(Pagamento.class);
        Pagamento confirmado2 = mock(Pagamento.class);
        Pagamento pendente = mock(Pagamento.class);

        when(orcamentoRepository.findById(orcamentoId))
                .thenReturn(Optional.of(orcamento));

        when(orcamento.getId())
                .thenReturn(orcamentoId);

        when(orcamento.getValorTotal())
                .thenReturn(new BigDecimal("1000.00"));

        when(confirmado1.getValor())
                .thenReturn(new BigDecimal("300.00"));

        when(confirmado2.getValor())
                .thenReturn(new BigDecimal("200.00"));

        when(pendente.getValor())
                .thenReturn(new BigDecimal("150.00"));

        when(pagamentoRepository.findByOrcamentoIdAndStatus(
                orcamentoId,
                StatusPagamento.CONFIRMADO
        )).thenReturn(List.of(confirmado1, confirmado2));

        when(pagamentoRepository.findByOrcamentoIdAndStatus(
                orcamentoId,
                StatusPagamento.PENDENTE
        )).thenReturn(List.of(pendente));

        PagamentoResumoResponseDTO resumo =
                pagamentoService.obterResumo(orcamentoId);

        assertEquals(
                new BigDecimal("1000.00"),
                resumo.valorTotal()
        );

        assertEquals(
                new BigDecimal("500.00"),
                resumo.totalPago()
        );

        assertEquals(
                new BigDecimal("150.00"),
                resumo.totalPendente()
        );

        assertEquals(
                new BigDecimal("500.00"),
                resumo.saldoRestante()
        );

        assertEquals(
                new BigDecimal("350.00"),
                resumo.valorDisponivelParaNovoPagamento()
        );
    }


    @Test
    void deveListarHistoricoDoPagamento() {

        Long pagamentoId = 1L;

        Pagamento pagamento = mock(Pagamento.class);

        when(pagamentoRepository.findById(pagamentoId))
                .thenReturn(Optional.of(pagamento));

        when(usuario.getId())
                .thenReturn(5L);

        when(usuario.getNome())
                .thenReturn("Usuário Teste");

        PagamentoHistorico historico =
                new PagamentoHistorico(
                        pagamento,
                        StatusPagamento.PENDENTE,
                        StatusPagamento.CONFIRMADO,
                        usuario
                );

        when(historicoRepository
                .findByPagamentoIdOrderByDataAlteracaoAsc(pagamentoId))
                .thenReturn(List.of(historico));

        List<PagamentoHistoricoResponseDTO> resultado =
                pagamentoService.listarHistorico(pagamentoId);

        assertEquals(1, resultado.size());

        PagamentoHistoricoResponseDTO response =
                resultado.get(0);

        assertEquals(
                StatusPagamento.PENDENTE,
                response.statusAnterior()
        );

        assertEquals(
                StatusPagamento.CONFIRMADO,
                response.statusNovo()
        );

        assertEquals(5L, response.usuarioId());
        assertEquals("Usuário Teste", response.usuarioNome());

        verify(pagamentoRepository).findById(pagamentoId);

        verify(historicoRepository)
                .findByPagamentoIdOrderByDataAlteracaoAsc(pagamentoId);
    }
}