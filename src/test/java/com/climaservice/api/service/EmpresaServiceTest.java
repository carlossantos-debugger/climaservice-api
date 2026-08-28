package com.climaservice.api.service;

import com.climaservice.api.dto.EmpresaAtualizarRequestDTO;
import com.climaservice.api.dto.EmpresaResponseDTO;
import com.climaservice.api.dto.RegisterCompanyRequestDTO;
import com.climaservice.api.dto.RegisterCompanyResponseDTO;
import com.climaservice.api.entity.Empresa;
import com.climaservice.api.entity.RoleUsuario;
import com.climaservice.api.entity.Usuario;
import com.climaservice.api.exception.BusinessRuleException;
import com.climaservice.api.repository.EmpresaRepository;
import com.climaservice.api.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    private static final Long EMPRESA_ID = 8001L;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private Empresa empresa;

    @InjectMocks
    private EmpresaService empresaService;


    @Test
    void deveRegistrarEmpresaEAdminQuandoDadosForemValidos() {

        RegisterCompanyRequestDTO dto = new RegisterCompanyRequestDTO("Empresa Nova", "12345678000199", "Admin Nova", "admin@empresanova.com", "senhaSegura123");

        when(usuarioRepository.existsByEmailIgnoreCase("admin@empresanova.com")).thenReturn(false);

        when(empresaRepository.existsByCpfCnpj("12345678000199")).thenReturn(false);

        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(passwordEncoder.encode("senhaSegura123")).thenReturn("hash-senha");

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtService.gerarToken(any(Usuario.class))).thenReturn("token-jwt");

        RegisterCompanyResponseDTO response = empresaService.registrarEmpresa(dto);

        assertNotNull(response);

        assertEquals("Empresa Nova", response.empresaNome());

        assertEquals("admin@empresanova.com", response.usuarioEmail());

        assertEquals(RoleUsuario.ADMIN, response.role());

        assertEquals("token-jwt", response.token());

        ArgumentCaptor<Empresa> empresaCaptor = ArgumentCaptor.forClass(Empresa.class);

        verify(empresaRepository).save(empresaCaptor.capture());

        assertTrue(empresaCaptor.getValue().isAtivo());

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario adminSalvo = usuarioCaptor.getValue();

        assertEquals(RoleUsuario.ADMIN, adminSalvo.getRole());

        assertEquals("hash-senha", adminSalvo.getSenhaHash());

        assertTrue(adminSalvo.getAtivo());
    }


    @Test
    void devePermitirCadastroSemCpfCnpjDaEmpresa() {

        RegisterCompanyRequestDTO dto = new RegisterCompanyRequestDTO("Empresa Sem Documento", null, "Admin", "admin@semdoc.com", "senhaSegura123");

        when(usuarioRepository.existsByEmailIgnoreCase("admin@semdoc.com")).thenReturn(false);

        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(passwordEncoder.encode("senhaSegura123")).thenReturn("hash-senha");

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtService.gerarToken(any(Usuario.class))).thenReturn("token-jwt");

        RegisterCompanyResponseDTO response = empresaService.registrarEmpresa(dto);

        assertNotNull(response);

        verify(empresaRepository, never()).existsByCpfCnpj(any());
    }


    @Test
    void deveImpedirCadastroComEmailDeAdminJaExistente() {

        RegisterCompanyRequestDTO dto = new RegisterCompanyRequestDTO("Empresa Nova", null, "Admin", "admin@existente.com", "senhaSegura123");

        when(usuarioRepository.existsByEmailIgnoreCase("admin@existente.com")).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> empresaService.registrarEmpresa(dto));

        assertEquals("Já existe um usuário cadastrado com este e-mail", exception.getMessage());

        verify(empresaRepository, never()).save(any(Empresa.class));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }


    @Test
    void deveImpedirCadastroComCpfCnpjDeEmpresaJaExistente() {

        RegisterCompanyRequestDTO dto = new RegisterCompanyRequestDTO("Empresa Nova", "12345678000199", "Admin", "admin@novo.com", "senhaSegura123");

        when(usuarioRepository.existsByEmailIgnoreCase("admin@novo.com")).thenReturn(false);

        when(empresaRepository.existsByCpfCnpj("12345678000199")).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> empresaService.registrarEmpresa(dto));

        assertEquals("Já existe uma empresa cadastrada com este CPF/CNPJ", exception.getMessage());

        verify(empresaRepository, never()).save(any(Empresa.class));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }


    @Test
    void deveObterEmpresaAtual() {

        when(usuarioAutenticadoService.obterEmpresaAtual()).thenReturn(empresa);

        when(empresa.getId()).thenReturn(EMPRESA_ID);

        when(empresa.getNome()).thenReturn("Empresa Atual");

        when(empresa.getCpfCnpj()).thenReturn("12345678000199");

        when(empresa.isAtivo()).thenReturn(true);

        EmpresaResponseDTO response = empresaService.obterEmpresaAtual();

        assertEquals(EMPRESA_ID, response.id());

        assertEquals("Empresa Atual", response.nome());

        assertTrue(response.ativo());
    }


    @Test
    void deveAtualizarEmpresaAtualQuandoCpfCnpjEstiverDisponivel() {

        EmpresaAtualizarRequestDTO dto = new EmpresaAtualizarRequestDTO("Empresa Renomeada", "98765432000188");

        when(usuarioAutenticadoService.obterEmpresaAtual()).thenReturn(empresa);

        when(empresa.getId()).thenReturn(EMPRESA_ID);

        when(empresaRepository.existsByCpfCnpjAndIdNot("98765432000188", EMPRESA_ID)).thenReturn(false);

        when(empresaRepository.save(empresa)).thenReturn(empresa);

        when(empresa.getNome()).thenReturn("Empresa Renomeada");

        when(empresa.getCpfCnpj()).thenReturn("98765432000188");

        when(empresa.isAtivo()).thenReturn(true);

        EmpresaResponseDTO response = empresaService.atualizarEmpresaAtual(dto);

        assertEquals("Empresa Renomeada", response.nome());

        verify(empresa).setNome("Empresa Renomeada");

        verify(empresa).setCpfCnpj("98765432000188");

        verify(empresaRepository).save(empresa);
    }


    @Test
    void deveImpedirAtualizacaoComCpfCnpjUsadoPorOutraEmpresa() {

        EmpresaAtualizarRequestDTO dto = new EmpresaAtualizarRequestDTO("Empresa Renomeada", "98765432000188");

        when(usuarioAutenticadoService.obterEmpresaAtual()).thenReturn(empresa);

        when(empresa.getId()).thenReturn(EMPRESA_ID);

        when(empresaRepository.existsByCpfCnpjAndIdNot("98765432000188", EMPRESA_ID)).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> empresaService.atualizarEmpresaAtual(dto));

        assertEquals("Já existe uma empresa cadastrada com este CPF/CNPJ", exception.getMessage());

        verify(empresaRepository, never()).save(any(Empresa.class));

        verify(empresa, never()).setNome(any());
    }
}
