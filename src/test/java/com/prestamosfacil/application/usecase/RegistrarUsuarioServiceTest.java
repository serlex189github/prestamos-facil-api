package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.port.out.UsuarioRepositoryPort;
import com.prestamosfacil.domain.exception.CorreoDuplicadoException;
import com.prestamosfacil.domain.exception.DocumentoDuplicadoException;
import com.prestamosfacil.domain.exception.SalarioInvalidoException;
import com.prestamosfacil.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    private RegistrarUsuarioService service;

    @BeforeEach
    void setUp() {
        service = new RegistrarUsuarioService(usuarioRepositoryPort);
    }

    @Test
    void debeLanzarNullPointerExceptionCuandoSalarioEsNulo() {
        // Este test documenta un BUG real: validarSalario() llama
        // salario.compareTo(...) sin comprobar null antes. Debería lanzar
        // SalarioInvalidoException de forma controlada, pero hoy revienta
        // con NPE. Cuando se corrija el servicio, cambiar esta aserción
        // a SalarioInvalidoException.
        Usuario usuario = crearUsuario(null);

        assertThrows(NullPointerException.class, () -> service.registrar(usuario));
    }

    @Test
    void debeFallarCuandoSalarioEsNegativo() {
        Usuario usuario = crearUsuario(new BigDecimal("-1"));
        assertThrows(SalarioInvalidoException.class, () -> service.registrar(usuario));
    }

    @Test
    void debeFallarCuandoSalarioSuperaElMaximo() {
        Usuario usuario = crearUsuario(new BigDecimal("15000000.01"));
        assertThrows(SalarioInvalidoException.class, () -> service.registrar(usuario));
    }

    @Test
    void debePermitirSalarioEnLosLimites() {
        Usuario usuarioLimiteInferior = crearUsuario(BigDecimal.ZERO);
        Usuario usuarioLimiteSuperior = crearUsuario(new BigDecimal("15000000"));

        when(usuarioRepositoryPort.existePorCorreo(any())).thenReturn(false);
        when(usuarioRepositoryPort.existePorNumeroDocumento(any())).thenReturn(false);
        when(usuarioRepositoryPort.guardar(any(Usuario.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        assertNotNull(service.registrar(usuarioLimiteInferior));
        assertNotNull(service.registrar(usuarioLimiteSuperior));
    }

    @Test
    void debeFallarCuandoCorreoYaEstaRegistrado() {
        Usuario usuario = crearUsuario(new BigDecimal("3000000"));
        when(usuarioRepositoryPort.existePorCorreo(usuario.getCorreo())).thenReturn(true);

        assertThrows(CorreoDuplicadoException.class, () -> service.registrar(usuario));
        verify(usuarioRepositoryPort, never()).existePorNumeroDocumento(any());
        verify(usuarioRepositoryPort, never()).guardar(any());
    }

    @Test
    void debeFallarCuandoDocumentoYaEstaRegistrado() {
        Usuario usuario = crearUsuario(new BigDecimal("3000000"));
        when(usuarioRepositoryPort.existePorCorreo(usuario.getCorreo())).thenReturn(false);
        when(usuarioRepositoryPort.existePorNumeroDocumento(usuario.getNumeroDocumento()))
            .thenReturn(true);

        assertThrows(DocumentoDuplicadoException.class, () -> service.registrar(usuario));
        verify(usuarioRepositoryPort, never()).guardar(any());
    }

    @Test
    void debeRegistrarUsuarioCorrectamenteConDatosValidos() {
        Usuario usuario = crearUsuario(new BigDecimal("3000000"));
        when(usuarioRepositoryPort.existePorCorreo(usuario.getCorreo())).thenReturn(false);
        when(usuarioRepositoryPort.existePorNumeroDocumento(usuario.getNumeroDocumento()))
            .thenReturn(false);
        when(usuarioRepositoryPort.guardar(any(Usuario.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = service.registrar(usuario);

        assertNotNull(resultado.getId());
        assertNotNull(resultado.getFechaCreacion());
        assertEquals(usuario.getCorreo(), resultado.getCorreo());
        assertEquals(usuario.getNumeroDocumento(), resultado.getNumeroDocumento());
        assertEquals(usuario.getSalarioBase(), resultado.getSalarioBase());
    }

    private Usuario crearUsuario(BigDecimal salarioBase) {
        return Usuario.builder()
            .nombres("Sergio")
            .apellidos("Gómez")
            .correo("sergio.gomez@example.com")
            .numeroDocumento("123456789")
            .salarioBase(salarioBase)
            .build();
    }
}
