package com.climaservice.api.service;

import com.climaservice.api.dto.ClienteRequestDTO;
import com.climaservice.api.dto.ClienteResponseDTO;
import com.climaservice.api.dto.EnderecoDTO;
import com.climaservice.api.dto.PageResponseDTO;
import com.climaservice.api.entity.Cliente;
import com.climaservice.api.entity.Empresa;
import com.climaservice.api.entity.Endereco;
import com.climaservice.api.repository.ClienteRepository;
import com.climaservice.api.repository.ClienteSpecifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

        cliente.setEndereco(converterParaEndereco(dto.endereco()));

        cliente.setInscricaoMunicipal(dto.inscricaoMunicipal());

        cliente.setInscricaoEstadual(dto.inscricaoEstadual());

        Cliente clienteSalvo =
                clienteRepository.save(cliente);

        return converterParaResponse(clienteSalvo);
    }

    public PageResponseDTO<ClienteResponseDTO> listar(String nome, String cpfCnpj, int page, int size) {

        Long empresaId =
                usuarioAutenticadoService
                        .obterEmpresaAtual()
                        .getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nome"));

        Page<ClienteResponseDTO> resultado = clienteRepository
                .findAll(ClienteSpecifications.comFiltros(empresaId, nome, cpfCnpj), pageable)
                .map(this::converterParaResponse);

        return PageResponseDTO.from(resultado);
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
                    cliente.setEndereco(converterParaEndereco(dto.endereco()));
                    cliente.setInscricaoMunicipal(dto.inscricaoMunicipal());
                    cliente.setInscricaoEstadual(dto.inscricaoEstadual());

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

    private Endereco converterParaEndereco(EnderecoDTO dto) {

        if (dto == null) {

            return null;
        }

        return new Endereco(dto.logradouro(), dto.numero(), dto.complemento(), dto.bairro(), dto.cidade(), dto.uf(), dto.cep());
    }

    private EnderecoDTO converterParaEnderecoDTO(Endereco endereco) {

        if (endereco == null) {

            return null;
        }

        return new EnderecoDTO(endereco.getLogradouro(), endereco.getNumero(), endereco.getComplemento(), endereco.getBairro(), endereco.getCidade(), endereco.getUf(), endereco.getCep());
    }

    private ClienteResponseDTO converterParaResponse(
            Cliente cliente
    ) {

        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpfCnpj(),
                cliente.getTelefone(),
                cliente.getEmail(),
                converterParaEnderecoDTO(cliente.getEndereco()),
                cliente.getInscricaoMunicipal(),
                cliente.getInscricaoEstadual()
        );
    }
}