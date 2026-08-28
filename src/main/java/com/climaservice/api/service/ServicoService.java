package com.climaservice.api.service;

import com.climaservice.api.dto.ServicoRequestDTO;
import com.climaservice.api.dto.ServicoResponseDTO;
import com.climaservice.api.entity.Empresa;
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
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public ServicoService(ServicoRepository servicoRepository, UsuarioAutenticadoService usuarioAutenticadoService) {
        this.servicoRepository = servicoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public ServicoResponseDTO salvar(ServicoRequestDTO dto) {

        Empresa empresa = usuarioAutenticadoService.obterEmpresaAtual();

        Servico servico = new Servico(dto.nome(), dto.descricao(), dto.valorPadrao(), empresa);

        Servico servicoSalvo = servicoRepository.save(servico);

        return converterParaResponse(servicoSalvo);
    }

    @Transactional(readOnly = true)
    public List<ServicoResponseDTO> listarTodos() {

        Long empresaId = obterEmpresaIdAtual();

        return servicoRepository.findByEmpresa_IdOrderByNomeAsc(empresaId).stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ServicoResponseDTO> listarAtivos() {

        Long empresaId = obterEmpresaIdAtual();

        return servicoRepository.findByEmpresa_IdAndAtivoTrueOrderByNomeAsc(empresaId).stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ServicoResponseDTO> buscarPorId(Long id) {

        Long empresaId = obterEmpresaIdAtual();

        return servicoRepository.findByIdAndEmpresa_Id(id, empresaId).map(this::converterParaResponse);
    }

    @Transactional
    public ServicoResponseDTO atualizar(Long id, ServicoRequestDTO dto) {

        Servico servico = buscarEntidadePorId(id);

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

        Long empresaId = obterEmpresaIdAtual();

        return servicoRepository.findByIdAndEmpresa_Id(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Serviço com ID " + id + " não encontrado"));
    }

    private Long obterEmpresaIdAtual() {

        return usuarioAutenticadoService.obterEmpresaAtual().getId();
    }

    private ServicoResponseDTO converterParaResponse(Servico servico) {

        return new ServicoResponseDTO(servico.getId(), servico.getNome(), servico.getDescricao(), servico.getValorPadrao(), servico.getAtivo());
    }
}