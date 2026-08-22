package com.climaservice.api.service;

import com.climaservice.api.dto.ProdutoRequestDTO;
import com.climaservice.api.dto.ProdutoResponseDTO;
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

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public ProdutoResponseDTO salvar(ProdutoRequestDTO dto) {

        Produto produto = new Produto(dto.nome(), dto.descricao(), dto.valorPadrao());

        Produto produtoSalvo = produtoRepository.save(produto);

        return converterParaResponse(produtoSalvo);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarTodos() {

        return produtoRepository.findAll().stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarAtivos() {

        return produtoRepository.findByAtivoTrueOrderByNomeAsc().stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProdutoResponseDTO> buscarPorId(Long id) {

        return produtoRepository.findById(id).map(this::converterParaResponse);
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

        return produtoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Produto com ID " + id + " não encontrado"));
    }

    private ProdutoResponseDTO converterParaResponse(Produto produto) {

        return new ProdutoResponseDTO(produto.getId(), produto.getNome(), produto.getDescricao(), produto.getValorPadrao(), produto.getAtivo());
    }
}