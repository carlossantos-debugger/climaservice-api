package com.climaservice.api.service;

import com.climaservice.api.dto.EquipamentoRequestDTO;
import com.climaservice.api.dto.EquipamentoResponseDTO;
import com.climaservice.api.entity.Cliente;
import com.climaservice.api.entity.Empresa;
import com.climaservice.api.entity.Equipamento;
import com.climaservice.api.entity.StatusEquipamento;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.ClienteRepository;
import com.climaservice.api.repository.EquipamentoRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public EquipamentoService(EquipamentoRepository equipamentoRepository, ClienteRepository clienteRepository, UsuarioAutenticadoService usuarioAutenticadoService) {
        this.equipamentoRepository = equipamentoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    public List<EquipamentoResponseDTO> listarTodos() {

        Long empresaId = obterEmpresaIdAtual();

        return equipamentoRepository.findByEmpresa_IdOrderByIdAsc(empresaId).stream().map(this::converterParaResponse).toList();
    }

    public Optional<EquipamentoResponseDTO> buscarPorId(Long id) {

        Long empresaId = obterEmpresaIdAtual();

        return equipamentoRepository.findByIdAndEmpresa_Id(id, empresaId).map(this::converterParaResponse);
    }

    public List<EquipamentoResponseDTO> listarPorCliente(Long clienteId) {

        Long empresaId = obterEmpresaIdAtual();

        /*
         * Primeiro garantimos que o cliente pertence
         * à empresa autenticada.
         */
        buscarClienteDaEmpresaAtual(clienteId, empresaId);

        return equipamentoRepository.findByCliente_IdAndEmpresa_Id(clienteId, empresaId).stream().map(this::converterParaResponse).toList();
    }

    public EquipamentoResponseDTO salvar(EquipamentoRequestDTO dto) {

        Empresa empresa = usuarioAutenticadoService.obterEmpresaAtual();

        Long empresaId = empresa.getId();

        Cliente cliente = buscarClienteDaEmpresaAtual(dto.clienteId(), empresaId);

        Equipamento equipamento = new Equipamento(dto.marca(), dto.modelo(), dto.capacidadeBtu(), dto.numeroSerie(), dto.localInstalacao(), cliente, empresa);

        Equipamento equipamentoSalvo = equipamentoRepository.save(equipamento);

        return converterParaResponse(equipamentoSalvo);
    }

    public Optional<EquipamentoResponseDTO> atualizar(Long id, EquipamentoRequestDTO dto) {

        Long empresaId = obterEmpresaIdAtual();

        return equipamentoRepository.findByIdAndEmpresa_Id(id, empresaId).map(equipamento -> {

            /*
             * Também impedimos que um equipamento
             * seja transferido para um cliente de
             * outra empresa.
             */
            Cliente cliente = buscarClienteDaEmpresaAtual(dto.clienteId(), empresaId);

            equipamento.setMarca(dto.marca());

            equipamento.setModelo(dto.modelo());

            equipamento.setCapacidadeBtu(dto.capacidadeBtu());

            equipamento.setNumeroSerie(dto.numeroSerie());

            equipamento.setLocalInstalacao(dto.localInstalacao());

            equipamento.setCliente(cliente);

            Equipamento equipamentoAtualizado = equipamentoRepository.save(equipamento);

            return converterParaResponse(equipamentoAtualizado);
        });
    }

    public List<EquipamentoResponseDTO> listarAtivosPorCliente(Long clienteId) {

        Long empresaId = obterEmpresaIdAtual();

        buscarClienteDaEmpresaAtual(clienteId, empresaId);

        return equipamentoRepository.findByCliente_IdAndStatusAndEmpresa_Id(clienteId, StatusEquipamento.ATIVO, empresaId).stream().map(this::converterParaResponse).toList();
    }

    public EquipamentoResponseDTO ativar(Long id) {

        Long empresaId = obterEmpresaIdAtual();

        Equipamento equipamento = equipamentoRepository.findByIdAndEmpresa_Id(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Equipamento com ID " + id + " não encontrado"));

        equipamento.setStatus(StatusEquipamento.ATIVO);

        Equipamento equipamentoAtualizado = equipamentoRepository.save(equipamento);

        return converterParaResponse(equipamentoAtualizado);
    }

    public EquipamentoResponseDTO inativar(Long id) {

        Long empresaId = obterEmpresaIdAtual();

        Equipamento equipamento = equipamentoRepository.findByIdAndEmpresa_Id(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Equipamento com ID " + id + " não encontrado"));

        equipamento.setStatus(StatusEquipamento.INATIVO);

        Equipamento equipamentoAtualizado = equipamentoRepository.save(equipamento);

        return converterParaResponse(equipamentoAtualizado);
    }

    private Cliente buscarClienteDaEmpresaAtual(Long clienteId, Long empresaId) {

        return clienteRepository.findByIdAndEmpresa_Id(clienteId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
    }

    private Long obterEmpresaIdAtual() {

        return usuarioAutenticadoService.obterEmpresaAtual().getId();
    }

    private EquipamentoResponseDTO converterParaResponse(Equipamento equipamento) {

        return new EquipamentoResponseDTO(equipamento.getId(), equipamento.getMarca(), equipamento.getModelo(), equipamento.getCapacidadeBtu(), equipamento.getNumeroSerie(), equipamento.getLocalInstalacao(), equipamento.getStatus(), equipamento.getCliente().getId(), equipamento.getCliente().getNome());
    }
}