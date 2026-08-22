package com.climaservice.api.service;

import com.climaservice.api.dto.ServicoRequestDTO;
import com.climaservice.api.dto.ServicoResponseDTO;
import com.climaservice.api.entity.Servico;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Transactional
    public ServicoResponseDTO salvar(ServicoRequestDTO dto) {

        Servico servico = new Servico(dto.nome(), dto.descricao(), dto.valorPadrao());

        Servico servicoSalvo = servicoRepository.save(servico);

        return converterParaResponse(servicoSalvo);
    }

    @Transactional(readOnly = true)
    public List<ServicoResponseDTO> listarTodos() {

        return servicoRepository.findAll().stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ServicoResponseDTO> listarAtivos() {

        return servicoRepository.findByAtivoTrueOrderByNomeAsc().stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ServicoResponseDTO> buscarPorId(Long id) {

        return servicoRepository.findById(id).map(this::converterParaResponse);
    }

    @Transactional
    public ServicoResponseDTO atualizar(Long id, ServicoRequestDTO dto) {

        Servico servico = servicoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Serviço com ID " + id + " não encontrado"));

        servico.setNome(dto.nome());
        servico.setDescricao(dto.descricao());
        servico.setValorPadrao(dto.valorPadrao());

        Servico servicoAtualizado = servicoRepository.save(servico);

        return converterParaResponse(servicoAtualizado);
    }

    @Transactional
    public ServicoResponseDTO inativar(Long id) {

        Servico servico = buscarEntidadePorId(id);

        servico.setAtivo(false);

        Servico servicoAtualizado = servicoRepository.save(servico);

        return converterParaResponse(servicoAtualizado);
    }

    @Transactional
    public ServicoResponseDTO ativar(Long id) {

        Servico servico = buscarEntidadePorId(id);

        servico.setAtivo(true);

        Servico servicoAtualizado = servicoRepository.save(servico);

        return converterParaResponse(servicoAtualizado);
    }

    private Servico buscarEntidadePorId(Long id) {

        return servicoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Serviço com ID " + id + " não encontrado"));
    }

    private ServicoResponseDTO converterParaResponse(Servico servico) {

        return new ServicoResponseDTO(servico.getId(), servico.getNome(), servico.getDescricao(), servico.getValorPadrao(), servico.getAtivo());
    }
}