package com.climaservice.api.controller;

import com.climaservice.api.dto.ProdutoRequestDTO;
import com.climaservice.api.dto.ProdutoResponseDTO;
import com.climaservice.api.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> salvar(@Valid @RequestBody ProdutoRequestDTO dto) {

        ProdutoResponseDTO produto = produtoService.salvar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(produto);
    }

    @GetMapping
    public List<ProdutoResponseDTO> listarTodos() {
        return produtoService.listarTodos();
    }

    @GetMapping("/ativos")
    public List<ProdutoResponseDTO> listarAtivos() {
        return produtoService.listarAtivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {

        return produtoService.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequestDTO dto) {

        return ResponseEntity.ok(produtoService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<ProdutoResponseDTO> inativar(@PathVariable Long id) {

        return ResponseEntity.ok(produtoService.inativar(id));
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<ProdutoResponseDTO> ativar(@PathVariable Long id) {

        return ResponseEntity.ok(produtoService.ativar(id));
    }
}