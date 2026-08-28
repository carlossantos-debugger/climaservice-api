package com.climaservice.api.service;

import com.climaservice.api.dto.ProdutoRequestDTO;
import com.climaservice.api.dto.ProdutoResponseDTO;
import com.climaservice.api.entity.Empresa;
import com.climaservice.api.entity.Produto;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.ProdutoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public ProdutoService(ProdutoRepository produtoRepository, UsuarioAutenticadoService usuarioAutenticadoService) {
        this.produtoRepository = produtoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public ProdutoResponseDTO salvar(ProdutoRequestDTO dto) {

        Empresa empresa = usuarioAutenticadoService.obterEmpresaAtual();

        Produto produto = new Produto(dto.nome(), dto.descricao(), dto.valorPadrao(), empresa);

        Produto produtoSalvo = produtoRepository.save(produto);

        return converterParaResponse(produtoSalvo);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarTodos() {

        Long empresaId = obterEmpresaIdAtual();

        return produtoRepository.findByEmpresa_IdOrderByNomeAsc(empresaId).stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarAtivos() {

        Long empresaId = obterEmpresaIdAtual();

        return produtoRepository.findByEmpresa_IdAndAtivoTrueOrderByNomeAsc(empresaId).stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProdutoResponseDTO> buscarPorId(Long id) {

        Long empresaId = obterEmpresaIdAtual();

        return produtoRepository.findByIdAndEmpresa_Id(id, empresaId).map(this::converterParaResponse);
    }

    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {

        Produto produto = buscarEntidadePorId(id);

        produto.setNome(dto.nome());

        produto.setDescricao(dto.descricao());

        produto.setValorPadrao(dto.valorPadrao());

        Produto produtoAtualizado = produtoRepository.save(produto);

        return converterParaResponse(produtoAtualizado);
    }

    @Transactional
    public ProdutoResponseDTO inativar(Long id) {

        Produto produto = buscarEntidadePorId(id);

        produto.setAtivo(false);

        Produto produtoAtualizado = produtoRepository.save(produto);

        return converterParaResponse(produtoAtualizado);
    }

    @Transactional
    public ProdutoResponseDTO ativar(Long id) {

        Produto produto = buscarEntidadePorId(id);

        produto.setAtivo(true);

        Produto produtoAtualizado = produtoRepository.save(produto);

        return converterParaResponse(produtoAtualizado);
    }

    private Produto buscarEntidadePorId(Long id) {

        Long empresaId = obterEmpresaIdAtual();

        return produtoRepository.findByIdAndEmpresa_Id(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + id + " não encontrado"));
    }

    private Long obterEmpresaIdAtual() {

        return usuarioAutenticadoService.obterEmpresaAtual().getId();
    }

    private ProdutoResponseDTO converterParaResponse(Produto produto) {

        return new ProdutoResponseDTO(produto.getId(), produto.getNome(), produto.getDescricao(), produto.getValorPadrao(), produto.getAtivo());
    }
}