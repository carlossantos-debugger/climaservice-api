package com.climaservice.api.service;

import com.climaservice.api.dto.ClienteRequestDTO;
import com.climaservice.api.dto.ClienteResponseDTO;
import com.climaservice.api.entity.Cliente;
import com.climaservice.api.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ClienteResponseDTO salvar(ClienteRequestDTO dto) {

        Cliente cliente = new Cliente(dto.nome(), dto.cpfCnpj(), dto.telefone(), dto.email());

        Cliente clienteSalvo = clienteRepository.save(cliente);

        return converterParaResponse(clienteSalvo);
    }

    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll().stream().map(this::converterParaResponse).toList();
    }

    public Optional<ClienteResponseDTO> buscarPorId(Long id) {
        return clienteRepository.findById(id).map(this::converterParaResponse);
    }

    public Optional<ClienteResponseDTO> atualizar(Long id, ClienteRequestDTO dto) {

        return clienteRepository.findById(id).map(cliente -> {

            cliente.setNome(dto.nome());
            cliente.setCpfCnpj(dto.cpfCnpj());
            cliente.setTelefone(dto.telefone());
            cliente.setEmail(dto.email());

            Cliente clienteAtualizado = clienteRepository.save(cliente);

            return converterParaResponse(clienteAtualizado);
        });
    }

    public void excluir(Long id) {
        clienteRepository.deleteById(id);
    }

    private ClienteResponseDTO converterParaResponse(Cliente cliente) {

        return new ClienteResponseDTO(cliente.getId(), cliente.getNome(), cliente.getCpfCnpj(), cliente.getTelefone(), cliente.getEmail());
    }
}