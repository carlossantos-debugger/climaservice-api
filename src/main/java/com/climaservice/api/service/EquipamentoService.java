package com.climaservice.api.service;

import com.climaservice.api.dto.EquipamentoRequestDTO;
import com.climaservice.api.dto.EquipamentoResponseDTO;
import com.climaservice.api.entity.Cliente;
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

    public EquipamentoService(EquipamentoRepository equipamentoRepository, ClienteRepository clienteRepository) {
        this.equipamentoRepository = equipamentoRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<EquipamentoResponseDTO> listarTodos() {

        return equipamentoRepository.findAll().stream().map(this::converterParaResponse).toList();
    }

    public Optional<EquipamentoResponseDTO> buscarPorId(Long id) {

        return equipamentoRepository.findById(id).map(this::converterParaResponse);
    }

    public List<EquipamentoResponseDTO> listarPorCliente(Long clienteId) {

        return equipamentoRepository.findByClienteId(clienteId).stream().map(this::converterParaResponse).toList();
    }

    public EquipamentoResponseDTO salvar(EquipamentoRequestDTO dto) {

        Cliente cliente = clienteRepository.findById(dto.clienteId()).orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        Equipamento equipamento = new Equipamento(dto.marca(), dto.modelo(), dto.capacidadeBtu(), dto.numeroSerie(), dto.localInstalacao(), cliente);

        Equipamento equipamentoSalvo = equipamentoRepository.save(equipamento);

        return converterParaResponse(equipamentoSalvo);
    }

    public Optional<EquipamentoResponseDTO> atualizar(Long id, EquipamentoRequestDTO dto) {

        return equipamentoRepository.findById(id).map(equipamento -> {

            Cliente cliente = clienteRepository.findById(dto.clienteId()).orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

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

        return equipamentoRepository.findByClienteIdAndStatus(clienteId, StatusEquipamento.ATIVO).stream().map(this::converterParaResponse).toList();
    }

    public EquipamentoResponseDTO ativar(Long id) {

        Equipamento equipamento = equipamentoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipamento com ID " + id + " não encontrado"));

        equipamento.setStatus(StatusEquipamento.ATIVO);

        Equipamento equipamentoAtualizado = equipamentoRepository.save(equipamento);

        return converterParaResponse(equipamentoAtualizado);
    }

    public EquipamentoResponseDTO inativar(Long id) {

        Equipamento equipamento = equipamentoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipamento com ID " + id + " não encontrado"));

        equipamento.setStatus(StatusEquipamento.INATIVO);

        Equipamento equipamentoAtualizado = equipamentoRepository.save(equipamento);

        return converterParaResponse(equipamentoAtualizado);
    }

    private EquipamentoResponseDTO converterParaResponse(Equipamento equipamento) {

        return new EquipamentoResponseDTO(equipamento.getId(), equipamento.getMarca(), equipamento.getModelo(), equipamento.getCapacidadeBtu(), equipamento.getNumeroSerie(), equipamento.getLocalInstalacao(), equipamento.getStatus(), equipamento.getCliente().getId(), equipamento.getCliente().getNome());
    }

}
