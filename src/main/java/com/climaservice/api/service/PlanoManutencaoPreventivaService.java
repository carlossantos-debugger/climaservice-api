package com.climaservice.api.service;

import com.climaservice.api.dto.OrdemServicoRequestDTO;
import com.climaservice.api.dto.OrdemServicoResponseDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaAtualizarRequestDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaExecucaoResponseDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaRequestDTO;
import com.climaservice.api.dto.PlanoManutencaoPreventivaResponseDTO;
import com.climaservice.api.entity.Empresa;
import com.climaservice.api.entity.Equipamento;
import com.climaservice.api.entity.OrdemServico;
import com.climaservice.api.entity.PlanoManutencaoPreventiva;
import com.climaservice.api.entity.PlanoManutencaoPreventivaExecucao;
import com.climaservice.api.entity.RoleUsuario;
import com.climaservice.api.entity.StatusEquipamento;
import com.climaservice.api.entity.Usuario;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.EquipamentoRepository;
import com.climaservice.api.repository.OrdemServicoRepository;
import com.climaservice.api.repository.PlanoManutencaoPreventivaExecucaoRepository;
import com.climaservice.api.repository.PlanoManutencaoPreventivaRepository;
import com.climaservice.api.repository.PlanoManutencaoPreventivaSpecifications;
import com.climaservice.api.repository.UsuarioRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PlanoManutencaoPreventivaService {

    private final PlanoManutencaoPreventivaRepository planoRepository;
    private final PlanoManutencaoPreventivaExecucaoRepository execucaoRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final OrdemServicoService ordemServicoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public PlanoManutencaoPreventivaService(PlanoManutencaoPreventivaRepository planoRepository, PlanoManutencaoPreventivaExecucaoRepository execucaoRepository, EquipamentoRepository equipamentoRepository, UsuarioRepository usuarioRepository, OrdemServicoRepository ordemServicoRepository, OrdemServicoService ordemServicoService, UsuarioAutenticadoService usuarioAutenticadoService) {

        this.planoRepository = planoRepository;
        this.execucaoRepository = execucaoRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.ordemServicoService = ordemServicoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public PlanoManutencaoPreventivaResponseDTO criar(PlanoManutencaoPreventivaRequestDTO dto) {

        Empresa empresa = usuarioAutenticadoService.obterEmpresaAtual();

        Long empresaId = empresa.getId();

        Equipamento equipamento = equipamentoRepository.findByIdAndEmpresa_Id(dto.equipamentoId(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Equipamento com ID " + dto.equipamentoId() + " não encontrado"));

        validarEquipamentoAtivo(equipamento);

        Usuario tecnicoPadrao = resolverTecnicoPadrao(dto.tecnicoPadraoId(), empresaId);

        LocalDate proximaExecucao = dto.proximaExecucao() != null ? dto.proximaExecucao() : LocalDate.now().plusMonths(dto.intervaloMeses());

        PlanoManutencaoPreventiva plano = new PlanoManutencaoPreventiva(equipamento, tecnicoPadrao, dto.intervaloMeses(), proximaExecucao, dto.observacao(), empresa);

        PlanoManutencaoPreventiva planoSalvo = planoRepository.save(plano);

        return converterParaResponse(planoSalvo);
    }

    private void validarEquipamentoAtivo(Equipamento equipamento) {

        if (equipamento.getStatus() != StatusEquipamento.ATIVO) {

            throw new BusinessRuleException("Não é possível criar um plano de manutenção preventiva para um equipamento inativo");
        }
    }

    private Usuario resolverTecnicoPadrao(Long tecnicoPadraoId, Long empresaId) {

        if (tecnicoPadraoId == null) {

            return null;
        }

        Usuario tecnico = usuarioRepository.findByIdAndEmpresa_Id(tecnicoPadraoId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Técnico com ID " + tecnicoPadraoId + " não encontrado"));

        validarTecnico(tecnico);

        return tecnico;
    }

    private void validarTecnico(Usuario tecnico) {

        if (tecnico.getRole() != RoleUsuario.TECNICO) {

            throw new BusinessRuleException("O usuário informado não possui perfil de técnico");
        }

        if (!Boolean.TRUE.equals(tecnico.getAtivo())) {

            throw new BusinessRuleException("O técnico informado está inativo");
        }
    }

    @Transactional
    public PlanoManutencaoPreventivaResponseDTO atualizar(Long id, PlanoManutencaoPreventivaAtualizarRequestDTO dto) {

        Long empresaId = obterEmpresaIdAtual();

        PlanoManutencaoPreventiva plano = buscarEntidadePorId(id);

        Usuario tecnicoPadrao = resolverTecnicoPadrao(dto.tecnicoPadraoId(), empresaId);

        plano.setTecnicoPadrao(tecnicoPadrao);

        plano.setIntervaloMeses(dto.intervaloMeses());

        plano.setProximaExecucao(dto.proximaExecucao());

        plano.setObservacao(dto.observacao());

        PlanoManutencaoPreventiva planoAtualizado = planoRepository.save(plano);

        return converterParaResponse(planoAtualizado);
    }

    @Transactional
    public PlanoManutencaoPreventivaResponseDTO ativar(Long id) {

        PlanoManutencaoPreventiva plano = buscarEntidadePorId(id);

        plano.setAtivo(true);

        return converterParaResponse(planoRepository.save(plano));
    }

    @Transactional
    public PlanoManutencaoPreventivaResponseDTO inativar(Long id) {

        PlanoManutencaoPreventiva plano = buscarEntidadePorId(id);

        plano.setAtivo(false);

        return converterParaResponse(planoRepository.save(plano));
    }

    @Transactional(readOnly = true)
    public List<PlanoManutencaoPreventivaResponseDTO> listar(Long equipamentoId, Boolean ativo) {

        Long empresaId = obterEmpresaIdAtual();

        return planoRepository.findAll(PlanoManutencaoPreventivaSpecifications.comFiltros(empresaId, equipamentoId, ativo), Sort.by(Sort.Direction.ASC, "proximaExecucao")).stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public PlanoManutencaoPreventivaResponseDTO buscarPorId(Long id) {

        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public List<PlanoManutencaoPreventivaResponseDTO> listarPorEquipamento(Long equipamentoId) {

        Long empresaId = obterEmpresaIdAtual();

        equipamentoRepository.findByIdAndEmpresa_Id(equipamentoId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Equipamento com ID " + equipamentoId + " não encontrado"));

        return planoRepository.findByEquipamento_IdAndEmpresa_IdOrderByProximaExecucaoAsc(equipamentoId, empresaId).stream().map(this::converterParaResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PlanoManutencaoPreventivaResponseDTO> listarProximas(int diasLimite) {

        Long empresaId = obterEmpresaIdAtual();

        LocalDate hoje = LocalDate.now();

        return planoRepository.findByEmpresa_IdAndAtivoTrueAndProximaExecucaoBetweenOrderByProximaExecucaoAsc(empresaId, hoje, hoje.plusDays(diasLimite)).stream().map(this::converterParaResponse).toList();
    }

    @Transactional
    public PlanoManutencaoPreventivaExecucaoResponseDTO gerarOrdemServico(Long id) {

        Long empresaId = obterEmpresaIdAtual();

        PlanoManutencaoPreventiva plano = buscarEntidadePorId(id);

        validarPlanoParaGeracao(plano);

        if (execucaoRepository.existsByPlano_IdAndDataReferencia(plano.getId(), plano.getProximaExecucao())) {

            throw new BusinessRuleException("Já existe uma ordem de serviço gerada para esta ocorrência de manutenção preventiva");
        }

        Equipamento equipamento = plano.getEquipamento();

        String descricaoProblema = "Manutenção preventiva programada" + (plano.getObservacao() != null && !plano.getObservacao().isBlank() ? " - " + plano.getObservacao() : "");

        OrdemServicoResponseDTO ordemServicoGerada = ordemServicoService.salvar(new OrdemServicoRequestDTO(equipamento.getCliente().getId(), equipamento.getId(), descricaoProblema));

        OrdemServico ordemServico = ordemServicoRepository.findByIdAndEmpresa_Id(ordemServicoGerada.id(), empresaId).orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço com ID " + ordemServicoGerada.id() + " não encontrada"));

        LocalDate dataReferencia = plano.getProximaExecucao();

        Usuario usuarioAtual = usuarioAutenticadoService.obterUsuarioAtual();

        PlanoManutencaoPreventivaExecucao execucao = new PlanoManutencaoPreventivaExecucao(plano, ordemServico, dataReferencia, usuarioAtual);

        PlanoManutencaoPreventivaExecucao execucaoSalva = execucaoRepository.save(execucao);

        plano.setUltimaExecucao(dataReferencia);

        plano.setProximaExecucao(dataReferencia.plusMonths(plano.getIntervaloMeses()));

        planoRepository.save(plano);

        return converterExecucaoParaResponse(execucaoSalva);
    }

    private void validarPlanoParaGeracao(PlanoManutencaoPreventiva plano) {

        if (!Boolean.TRUE.equals(plano.getAtivo())) {

            throw new BusinessRuleException("O plano de manutenção preventiva está inativo");
        }

        if (plano.getEquipamento().getStatus() != StatusEquipamento.ATIVO) {

            throw new BusinessRuleException("Não é possível gerar ordem de serviço para um equipamento inativo");
        }

        if (plano.getProximaExecucao().isAfter(LocalDate.now())) {

            throw new BusinessRuleException("A manutenção preventiva ainda não está no prazo de execução");
        }
    }

    @Transactional(readOnly = true)
    public List<PlanoManutencaoPreventivaExecucaoResponseDTO> listarExecucoes(Long planoId) {

        buscarEntidadePorId(planoId);

        return execucaoRepository.findByPlano_IdOrderByDataExecucaoAsc(planoId).stream().map(this::converterExecucaoParaResponse).toList();
    }

    private PlanoManutencaoPreventiva buscarEntidadePorId(Long id) {

        Long empresaId = obterEmpresaIdAtual();

        return planoRepository.findByIdAndEmpresa_Id(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Plano de manutenção preventiva com ID " + id + " não encontrado"));
    }

    private Long obterEmpresaIdAtual() {

        return usuarioAutenticadoService.obterEmpresaAtual().getId();
    }

    private PlanoManutencaoPreventivaResponseDTO converterParaResponse(PlanoManutencaoPreventiva plano) {

        Usuario tecnicoPadrao = plano.getTecnicoPadrao();

        return new PlanoManutencaoPreventivaResponseDTO(plano.getId(), plano.getEquipamento().getId(), plano.getEquipamento().getMarca(), plano.getEquipamento().getModelo(), tecnicoPadrao != null ? tecnicoPadrao.getId() : null, tecnicoPadrao != null ? tecnicoPadrao.getNome() : null, plano.getIntervaloMeses(), plano.getProximaExecucao(), plano.getUltimaExecucao(), plano.getAtivo(), plano.getObservacao(), plano.getDataCriacao());
    }

    private PlanoManutencaoPreventivaExecucaoResponseDTO converterExecucaoParaResponse(PlanoManutencaoPreventivaExecucao execucao) {

        Usuario usuario = execucao.getUsuario();

        return new PlanoManutencaoPreventivaExecucaoResponseDTO(execucao.getId(), execucao.getPlano().getId(), execucao.getOrdemServico().getId(), execucao.getDataReferencia(), execucao.getDataExecucao(), usuario != null ? usuario.getId() : null, usuario != null ? usuario.getNome() : null);
    }
}
