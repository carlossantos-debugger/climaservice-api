package com.climaservice.api.service;

import com.climaservice.api.dto.AtualizarDiagnosticoRequestDTO;
import com.climaservice.api.dto.AtualizarStatusOrdemServicoRequestDTO;
import com.climaservice.api.dto.OrdemServicoRequestDTO;
import com.climaservice.api.dto.OrdemServicoResponseDTO;
import com.climaservice.api.entity.*;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.ClienteRepository;
import com.climaservice.api.repository.EquipamentoRepository;
import com.climaservice.api.repository.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final EquipamentoRepository equipamentoRepository;

    public OrdemServicoService(
            OrdemServicoRepository ordemServicoRepository,
            ClienteRepository clienteRepository,
            EquipamentoRepository equipamentoRepository) {

        this.ordemServicoRepository = ordemServicoRepository;
        this.clienteRepository = clienteRepository;
        this.equipamentoRepository = equipamentoRepository;
    }

    @Transactional
    public OrdemServicoResponseDTO salvar(OrdemServicoRequestDTO dto) {

        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cliente com ID "
                                        + dto.clienteId()
                                        + " não encontrado"
                        )
                );

        Equipamento equipamento =
                equipamentoRepository.findById(dto.equipamentoId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Equipamento com ID "
                                                + dto.equipamentoId()
                                                + " não encontrado"
                                )
                        );

        validarEquipamentoDoCliente(cliente, equipamento);

        validarEquipamentoAtivo(equipamento);

        OrdemServico ordemServico = new OrdemServico(
                cliente,
                equipamento,
                dto.descricaoProblema()
        );

        OrdemServico ordemServicoSalva =
                ordemServicoRepository.save(ordemServico);

        return converterParaResponse(ordemServicoSalva);
    }

    private void validarEquipamentoDoCliente(
            Cliente cliente,
            Equipamento equipamento) {

        if (!equipamento.getCliente().getId().equals(cliente.getId())) {

            throw new BusinessRuleException(
                    "O equipamento informado não pertence ao cliente"
            );
        }
    }

    private void validarEquipamentoAtivo(
            Equipamento equipamento) {

        if (equipamento.getStatus() != StatusEquipamento.ATIVO) {

            throw new BusinessRuleException(
                    "Não é possível abrir uma ordem de serviço "
                            + "para um equipamento inativo"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarTodas() {

        return ordemServicoRepository.findAll()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<OrdemServicoResponseDTO> buscarPorId(Long id) {

        return ordemServicoRepository.findById(id)
                .map(this::converterParaResponse);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarPorCliente(
            Long clienteId) {

        return ordemServicoRepository
                .findByClienteId(clienteId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> listarPorEquipamento(
            Long equipamentoId) {

        return ordemServicoRepository
                .findByEquipamentoId(equipamentoId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional
    public OrdemServicoResponseDTO atualizarDiagnostico(
            Long id,
            AtualizarDiagnosticoRequestDTO dto) {

        OrdemServico ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ordem de serviço com ID " + id + " não encontrada"
                        )
                );

        if (ordemServico.getStatus() == StatusOrdemServico.CANCELADA) {
            throw new BusinessRuleException(
                    "Não é possível alterar o diagnóstico de uma ordem de serviço cancelada"
            );
        }

        if (ordemServico.getStatus() == StatusOrdemServico.CONCLUIDA) {
            throw new BusinessRuleException(
                    "Não é possível alterar o diagnóstico de uma ordem de serviço concluída"
            );
        }

        ordemServico.setDiagnostico(dto.diagnostico());

        OrdemServico ordemServicoAtualizada =
                ordemServicoRepository.save(ordemServico);

        return converterParaResponse(ordemServicoAtualizada);
    }

    private void validarTransicaoStatus(
            StatusOrdemServico atual,
            StatusOrdemServico novo) {

        boolean transicaoValida = switch (atual) {

            case ABERTA -> novo == StatusOrdemServico.EM_ANDAMENTO
                    || novo == StatusOrdemServico.CANCELADA;

            case EM_ANDAMENTO -> novo == StatusOrdemServico.AGUARDANDO_CLIENTE
                    || novo == StatusOrdemServico.CONCLUIDA
                    || novo == StatusOrdemServico.CANCELADA;

            case AGUARDANDO_CLIENTE -> novo == StatusOrdemServico.EM_ANDAMENTO
                    || novo == StatusOrdemServico.CANCELADA;

            case CONCLUIDA, CANCELADA -> false;
        };

        if (!transicaoValida) {
            throw new BusinessRuleException(
                    "Transição de status inválida: "
                            + atual
                            + " -> "
                            + novo
            );
        }
    }

    @Transactional
    public OrdemServicoResponseDTO atualizarStatus(
            Long id,
            AtualizarStatusOrdemServicoRequestDTO dto) {

        OrdemServico ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ordem de serviço com ID " + id + " não encontrada"
                        )
                );

        StatusOrdemServico novoStatus = dto.status();

        validarTransicaoStatus(
                ordemServico.getStatus(),
                novoStatus
        );

        ordemServico.setStatus(novoStatus);

        if (novoStatus == StatusOrdemServico.CONCLUIDA) {
            ordemServico.setDataConclusao(LocalDateTime.now());
        }

        OrdemServico ordemServicoAtualizada =
                ordemServicoRepository.save(ordemServico);

        return converterParaResponse(ordemServicoAtualizada);
    }

    private OrdemServicoResponseDTO converterParaResponse(
            OrdemServico ordemServico) {

        return new OrdemServicoResponseDTO(
                ordemServico.getId(),

                ordemServico.getCliente().getId(),
                ordemServico.getCliente().getNome(),

                ordemServico.getEquipamento().getId(),
                ordemServico.getEquipamento().getMarca(),
                ordemServico.getEquipamento().getModelo(),

                ordemServico.getDescricaoProblema(),
                ordemServico.getDiagnostico(),

                ordemServico.getStatus(),

                ordemServico.getDataAbertura(),
                ordemServico.getDataConclusao()
        );
    }

}