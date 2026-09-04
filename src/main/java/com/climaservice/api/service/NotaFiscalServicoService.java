package com.climaservice.api.service;

import com.climaservice.api.dto.NotaFiscalServicoRequestDTO;
import com.climaservice.api.dto.NotaFiscalServicoResponseDTO;
import com.climaservice.api.dto.PageResponseDTO;
import com.climaservice.api.entity.AmbienteNotaFiscal;
import com.climaservice.api.entity.Cliente;
import com.climaservice.api.entity.Empresa;
import com.climaservice.api.entity.Endereco;
import com.climaservice.api.entity.NotaFiscalServico;
import com.climaservice.api.entity.Orcamento;
import com.climaservice.api.entity.OrdemServico;
import com.climaservice.api.entity.StatusNotaFiscalServico;
import com.climaservice.api.entity.StatusOrcamento;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.exception.ResourceNotFoundException;
import com.climaservice.api.repository.NotaFiscalServicoRepository;
import com.climaservice.api.repository.NotaFiscalServicoSpecifications;
import com.climaservice.api.repository.OrcamentoRepository;
import com.climaservice.api.repository.OrdemServicoRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotaFiscalServicoService {

    private final NotaFiscalServicoRepository notaFiscalServicoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    /*
     * Instanciado diretamente (não injetado): este projeto usa
     * spring-boot-starter-webmvc sem spring-boot-starter-json, então
     * não há um bean ObjectMapper gerenciado pelo Spring disponível.
     * Uso aqui é puramente interno — o payload nunca é serializado
     * como resposta HTTP direta, só embutido como String dentro de
     * um DTO que já passa pelo pipeline JSON existente da aplicação.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AmbienteNotaFiscal ambienteConfigurado;

    public NotaFiscalServicoService(NotaFiscalServicoRepository notaFiscalServicoRepository, OrdemServicoRepository ordemServicoRepository, OrcamentoRepository orcamentoRepository, UsuarioAutenticadoService usuarioAutenticadoService, @Value("${app.nota-fiscal.ambiente:HOMOLOGACAO}") String ambienteConfigurado) {

        this.notaFiscalServicoRepository = notaFiscalServicoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.ambienteConfigurado = AmbienteNotaFiscal.valueOf(ambienteConfigurado);
    }

    @Transactional
    public NotaFiscalServicoResponseDTO criar(Long ordemServicoId, NotaFiscalServicoRequestDTO dto) {

        Empresa empresa = usuarioAutenticadoService.obterEmpresaAtual();

        Long empresaId = empresa.getId();

        OrdemServico ordemServico = ordemServicoRepository.findByIdAndEmpresa_Id(ordemServicoId, empresaId).orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço com ID " + ordemServicoId + " não encontrada"));

        if (notaFiscalServicoRepository.existsByOrdemServico_IdAndEmpresa_IdAndStatusNot(ordemServicoId, empresaId, StatusNotaFiscalServico.CANCELADA)) {

            throw new BusinessRuleException("Já existe uma nota fiscal ativa para esta ordem de serviço");
        }

        Orcamento orcamentoAprovado = buscarOrcamentoAprovado(ordemServicoId, empresaId);

        validarCadastroFiscalCompleto(empresa, ordemServico.getCliente());

        String codigoServico = dto.codigoServico() != null ? dto.codigoServico() : empresa.getCodigoServicoPadrao();

        BigDecimal aliquotaIss = dto.aliquotaIss() != null ? dto.aliquotaIss() : empresa.getAliquotaIssPadrao();

        BigDecimal valorServico = orcamentoAprovado.getValorTotal();

        BigDecimal valorIss = calcularValorIss(valorServico, aliquotaIss);

        NotaFiscalServico nota = new NotaFiscalServico(ordemServico, orcamentoAprovado, dto.discriminacaoServico(), codigoServico, aliquotaIss, valorServico, valorIss, ambienteConfigurado, empresa);

        NotaFiscalServico notaSalva = notaFiscalServicoRepository.save(nota);

        return converterParaResponse(notaSalva);
    }

    private Orcamento buscarOrcamentoAprovado(Long ordemServicoId, Long empresaId) {

        return orcamentoRepository.findByOrdemServico_IdAndEmpresa_IdOrderByDataCriacaoDesc(ordemServicoId, empresaId).stream().filter(orcamento -> orcamento.getStatus() == StatusOrcamento.APROVADO).findFirst().orElseThrow(() -> new BusinessRuleException("A ordem de serviço não possui orçamento aprovado"));
    }

    private void validarCadastroFiscalCompleto(Empresa empresa, Cliente cliente) {

        List<String> pendencias = new ArrayList<>();

        if (empresa.getEndereco() == null || !empresa.getEndereco().estaCompleto()) {

            pendencias.add("endereço da empresa");
        }

        if (empresa.getRegimeTributario() == null) {

            pendencias.add("regime tributário da empresa");
        }

        if (cliente.getEndereco() == null || !cliente.getEndereco().estaCompleto()) {

            pendencias.add("endereço do cliente");
        }

        if (!pendencias.isEmpty()) {

            throw new BusinessRuleException("Cadastro fiscal incompleto. Campos pendentes: " + String.join(", ", pendencias));
        }
    }

    private BigDecimal calcularValorIss(BigDecimal valorServico, BigDecimal aliquotaIss) {

        if (aliquotaIss == null) {

            return null;
        }

        return valorServico.multiply(aliquotaIss).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public NotaFiscalServicoResponseDTO atualizar(Long id, NotaFiscalServicoRequestDTO dto) {

        NotaFiscalServico nota = buscarEntidadePorId(id);

        validarRascunho(nota, "Somente notas em rascunho podem ser alteradas");

        nota.setDiscriminacaoServico(dto.discriminacaoServico());

        nota.setCodigoServico(dto.codigoServico() != null ? dto.codigoServico() : nota.getEmpresa().getCodigoServicoPadrao());

        BigDecimal aliquotaIss = dto.aliquotaIss() != null ? dto.aliquotaIss() : nota.getEmpresa().getAliquotaIssPadrao();

        nota.setAliquotaIss(aliquotaIss);

        nota.setValorIss(calcularValorIss(nota.getValorServico(), aliquotaIss));

        NotaFiscalServico notaAtualizada = notaFiscalServicoRepository.save(nota);

        return converterParaResponse(notaAtualizada);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<NotaFiscalServicoResponseDTO> listar(StatusNotaFiscalServico status, int page, int size) {

        Long empresaId = obterEmpresaIdAtual();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataCriacao"));

        Page<NotaFiscalServicoResponseDTO> resultado = notaFiscalServicoRepository.findAll(NotaFiscalServicoSpecifications.comFiltros(empresaId, status), pageable).map(this::converterParaResponse);

        return PageResponseDTO.from(resultado);
    }

    @Transactional(readOnly = true)
    public NotaFiscalServicoResponseDTO buscarPorId(Long id) {

        return converterParaResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public NotaFiscalServicoResponseDTO gerarPayload(Long id) {

        NotaFiscalServico nota = buscarEntidadePorId(id);

        validarRascunho(nota, "Somente notas em rascunho podem ter o payload gerado");

        String payload = montarPayload(nota);

        nota.setPayloadMontado(payload);

        NotaFiscalServico notaAtualizada = notaFiscalServicoRepository.save(nota);

        return converterParaResponse(notaAtualizada);
    }

    private String montarPayload(NotaFiscalServico nota) {

        Empresa empresa = nota.getEmpresa();

        Cliente cliente = nota.getOrdemServico().getCliente();

        Prestador prestador = new Prestador(empresa.getNome(), empresa.getCpfCnpj(), empresa.getInscricaoMunicipal(), converterEnderecoPayload(empresa.getEndereco()), empresa.getRegimeTributario() != null ? empresa.getRegimeTributario().name() : null);

        Tomador tomador = new Tomador(cliente.getNome(), cliente.getCpfCnpj(), converterEnderecoPayload(cliente.getEndereco()));

        ServicoPayload servico = new ServicoPayload(nota.getDiscriminacaoServico(), nota.getCodigoServico(), nota.getAliquotaIss());

        Valores valores = new Valores(nota.getValorServico(), nota.getValorIss());

        PayloadNotaFiscal payload = new PayloadNotaFiscal(prestador, tomador, servico, valores);

        try {

            return objectMapper.writeValueAsString(payload);

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException("Falha ao montar o payload da nota fiscal", exception);
        }
    }

    private EnderecoPayload converterEnderecoPayload(Endereco endereco) {

        if (endereco == null) {

            return null;
        }

        return new EnderecoPayload(endereco.getLogradouro(), endereco.getNumero(), endereco.getComplemento(), endereco.getBairro(), endereco.getCidade(), endereco.getUf(), endereco.getCep());
    }

    @Transactional
    public NotaFiscalServicoResponseDTO enviar(Long id) {

        NotaFiscalServico nota = buscarEntidadePorId(id);

        validarRascunho(nota, "Somente notas em rascunho podem ser enviadas");

        throw new BusinessRuleException("Envio à prefeitura ainda não disponível nesta instalação — requer certificado digital A1/A3 configurado (Fase 2)");
    }

    @Transactional
    public NotaFiscalServicoResponseDTO cancelar(Long id) {

        NotaFiscalServico nota = buscarEntidadePorId(id);

        validarRascunho(nota, "Somente notas em rascunho podem ser canceladas nesta fase");

        nota.setStatus(StatusNotaFiscalServico.CANCELADA);

        NotaFiscalServico notaAtualizada = notaFiscalServicoRepository.save(nota);

        return converterParaResponse(notaAtualizada);
    }

    private void validarRascunho(NotaFiscalServico nota, String mensagem) {

        if (nota.getStatus() != StatusNotaFiscalServico.RASCUNHO) {

            throw new BusinessRuleException(mensagem);
        }
    }

    private NotaFiscalServico buscarEntidadePorId(Long id) {

        Long empresaId = obterEmpresaIdAtual();

        return notaFiscalServicoRepository.findByIdAndEmpresa_Id(id, empresaId).orElseThrow(() -> new ResourceNotFoundException("Nota fiscal de serviço com ID " + id + " não encontrada"));
    }

    private Long obterEmpresaIdAtual() {

        return usuarioAutenticadoService.obterEmpresaAtual().getId();
    }

    private NotaFiscalServicoResponseDTO converterParaResponse(NotaFiscalServico nota) {

        return new NotaFiscalServicoResponseDTO(nota.getId(), nota.getOrdemServico().getId(), nota.getOrcamento().getId(), nota.getStatus(), nota.getAmbiente(), nota.getDiscriminacaoServico(), nota.getCodigoServico(), nota.getAliquotaIss(), nota.getValorServico(), nota.getValorIss(), nota.getNumeroNota(), nota.getCodigoVerificacao(), nota.getMotivoRejeicao(), nota.getDataEmissao(), nota.getPayloadMontado(), nota.getDataCriacao());
    }

    /*
     * Representação JSON interna provisória — NÃO é o XML/DPS oficial
     * do Sistema Nacional de NFS-e. Ver nota em NotaFiscalServico.payloadMontado.
     */
    private record PayloadNotaFiscal(Prestador prestador, Tomador tomador, ServicoPayload servico, Valores valores) {
    }

    private record Prestador(String nome, String cpfCnpj, String inscricaoMunicipal, EnderecoPayload endereco, String regimeTributario) {
    }

    private record Tomador(String nome, String cpfCnpj, EnderecoPayload endereco) {
    }

    private record EnderecoPayload(String logradouro, String numero, String complemento, String bairro, String cidade, String uf, String cep) {
    }

    private record ServicoPayload(String discriminacao, String codigoServico, BigDecimal aliquotaIss) {
    }

    private record Valores(BigDecimal valorServico, BigDecimal valorIss) {
    }
}
