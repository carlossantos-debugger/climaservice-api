package com.climaservice.api.service;

import com.climaservice.api.dto.EmpresaAtualizarRequestDTO;
import com.climaservice.api.dto.EmpresaResponseDTO;
import com.climaservice.api.dto.EnderecoDTO;
import com.climaservice.api.dto.RegisterCompanyRequestDTO;
import com.climaservice.api.dto.RegisterCompanyResponseDTO;
import com.climaservice.api.entity.Empresa;
import com.climaservice.api.entity.Endereco;
import com.climaservice.api.entity.RoleUsuario;
import com.climaservice.api.entity.Usuario;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.repository.EmpresaRepository;
import com.climaservice.api.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public EmpresaService(EmpresaRepository empresaRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService, UsuarioAutenticadoService usuarioAutenticadoService) {

        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public RegisterCompanyResponseDTO registrarEmpresa(RegisterCompanyRequestDTO dto) {

        validarEmailDisponivel(dto.adminEmail());

        validarCpfCnpjDisponivel(dto.empresaCpfCnpj());

        Empresa empresa = new Empresa(dto.empresaNome().trim(), dto.empresaCpfCnpj());

        Empresa empresaSalva = empresaRepository.save(empresa);

        String senhaHash = passwordEncoder.encode(dto.adminSenha());

        Usuario admin = new Usuario(dto.adminNome().trim(), dto.adminEmail(), senhaHash, RoleUsuario.ADMIN, empresaSalva);

        Usuario adminSalvo = usuarioRepository.save(admin);

        String token = jwtService.gerarToken(adminSalvo);

        return new RegisterCompanyResponseDTO(empresaSalva.getId(), empresaSalva.getNome(), adminSalvo.getId(), adminSalvo.getNome(), adminSalvo.getEmail(), adminSalvo.getRole(), token);
    }

    private void validarEmailDisponivel(String email) {

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {

            throw new BusinessRuleException("Já existe um usuário cadastrado com este e-mail");
        }
    }

    private void validarCpfCnpjDisponivel(String cpfCnpj) {

        if (cpfCnpj != null && empresaRepository.existsByCpfCnpj(cpfCnpj)) {

            throw new BusinessRuleException("Já existe uma empresa cadastrada com este CPF/CNPJ");
        }
    }

    @Transactional(readOnly = true)
    public EmpresaResponseDTO obterEmpresaAtual() {

        Empresa empresa = usuarioAutenticadoService.obterEmpresaAtual();

        return converterParaResponse(empresa);
    }

    @Transactional
    public EmpresaResponseDTO atualizarEmpresaAtual(EmpresaAtualizarRequestDTO dto) {

        Empresa empresa = usuarioAutenticadoService.obterEmpresaAtual();

        if (dto.cpfCnpj() != null && empresaRepository.existsByCpfCnpjAndIdNot(dto.cpfCnpj(), empresa.getId())) {

            throw new BusinessRuleException("Já existe uma empresa cadastrada com este CPF/CNPJ");
        }

        empresa.setNome(dto.nome().trim());

        empresa.setCpfCnpj(dto.cpfCnpj());

        empresa.setEndereco(converterParaEndereco(dto.endereco()));

        empresa.setInscricaoMunicipal(dto.inscricaoMunicipal());

        empresa.setRegimeTributario(dto.regimeTributario());

        empresa.setCodigoServicoPadrao(dto.codigoServicoPadrao());

        empresa.setAliquotaIssPadrao(dto.aliquotaIssPadrao());

        Empresa empresaAtualizada = empresaRepository.save(empresa);

        return converterParaResponse(empresaAtualizada);
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

    private EmpresaResponseDTO converterParaResponse(Empresa empresa) {

        return new EmpresaResponseDTO(empresa.getId(), empresa.getNome(), empresa.getCpfCnpj(), empresa.isAtivo(), empresa.getDataCriacao(), converterParaEnderecoDTO(empresa.getEndereco()), empresa.getInscricaoMunicipal(), empresa.getRegimeTributario(), empresa.getCodigoServicoPadrao(), empresa.getAliquotaIssPadrao());
    }
}
