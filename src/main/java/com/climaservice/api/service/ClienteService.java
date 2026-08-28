package com.climaservice.api.service;

import com.climaservice.api.dto.ClienteRequestDTO;
import com.climaservice.api.dto.ClienteResponseDTO;
import com.climaservice.api.entity.Cliente;
import com.climaservice.api.entity.Empresa;
import com.climaservice.api.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public ClienteService(
            ClienteRepository clienteRepository,
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.clienteRepository = clienteRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    public ClienteResponseDTO salvar(ClienteRequestDTO dto) {

        Empresa empresa =
                usuarioAutenticadoService.obterEmpresaAtual();

        Cliente cliente = new Cliente(
                dto.nome().trim(),
                dto.cpfCnpj(),
                dto.telefone(),
                dto.email(),
                empresa
        );

        Cliente clienteSalvo =
                clienteRepository.save(cliente);

        return converterParaResponse(clienteSalvo);
    }

    public List<ClienteResponseDTO> listarTodos() {

        Long empresaId =
                usuarioAutenticadoService
                        .obterEmpresaAtual()
                        .getId();

        return clienteRepository
                .findByEmpresa_IdOrderByNomeAsc(empresaId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public Optional<ClienteResponseDTO> buscarPorId(Long id) {

        Long empresaId =
                usuarioAutenticadoService
                        .obterEmpresaAtual()
                        .getId();

        return clienteRepository
                .findByIdAndEmpresa_Id(id, empresaId)
                .map(this::converterParaResponse);
    }

    public Optional<ClienteResponseDTO> atualizar(
            Long id,
            ClienteRequestDTO dto
    ) {

        Long empresaId =
                usuarioAutenticadoService
                        .obterEmpresaAtual()
                        .getId();

        return clienteRepository
                .findByIdAndEmpresa_Id(id, empresaId)
                .map(cliente -> {

                    cliente.setNome(dto.nome().trim());
                    cliente.setCpfCnpj(dto.cpfCnpj());
                    cliente.setTelefone(dto.telefone());
                    cliente.setEmail(dto.email());

                    Cliente clienteAtualizado =
                            clienteRepository.save(cliente);

                    return converterParaResponse(clienteAtualizado);
                });
    }

    public void excluir(Long id) {

        Long empresaId =
                usuarioAutenticadoService
                        .obterEmpresaAtual()
                        .getId();

        clienteRepository
                .findByIdAndEmpresa_Id(id, empresaId)
                .ifPresent(clienteRepository::delete);
    }

    private ClienteResponseDTO converterParaResponse(
            Cliente cliente
    ) {

        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpfCnpj(),
                cliente.getTelefone(),
                cliente.getEmail()
        );
    }
}