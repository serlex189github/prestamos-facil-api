package com.prestamosfacil.infrastructure.adapter.in.rest;

import com.prestamosfacil.application.port.out.CuotaPlanPagoRepositoryPort;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.enums.TipoDocumento;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.SolicitudPrestamoEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.TipoPrestamoEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.CuotaPlanPagoJpaRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.PrestamoJpaRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.SolicitudPrestamoJpaRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.TipoPrestamoJpaRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class EvaluacionManualRollbackIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("prestamos_facil_test")
            .withUsername("prestamos")
            .withPassword("prestamos");

    @DynamicPropertySource
    static void configureProperties(
        DynamicPropertyRegistry registry
    ) {
        registry.add(
            "spring.datasource.url",
            POSTGRES::getJdbcUrl
        );

        registry.add(
            "spring.datasource.username",
            POSTGRES::getUsername
        );

        registry.add(
            "spring.datasource.password",
            POSTGRES::getPassword
        );
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SolicitudPrestamoJpaRepository solicitudPrestamoJpaRepository;

    @Autowired
    private TipoPrestamoJpaRepository tipoPrestamoJpaRepository;

    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;

    @Autowired
    private PrestamoJpaRepository prestamoJpaRepository;

    @Autowired
    private CuotaPlanPagoJpaRepository cuotaPlanPagoJpaRepository;

    /*
     * Reemplaza el adaptador real dentro del contexto de Spring.
     * De esta manera provocamos una excepción exactamente cuando
     * el servicio intenta guardar las cuotas.
     */
    @MockitoBean
    private CuotaPlanPagoRepositoryPort cuotaPlanPagoRepositoryPort;

    private UUID solicitudId;
    private UUID usuarioId;
    private UUID tipoPrestamoId;

    @BeforeEach
    void setUp() {
        cuotaPlanPagoJpaRepository.deleteAll();
        prestamoJpaRepository.deleteAll();
        solicitudPrestamoJpaRepository.deleteAll();
        tipoPrestamoJpaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();

        usuarioId = UUID.randomUUID();
        solicitudId = UUID.randomUUID();
        tipoPrestamoId = UUID.randomUUID();

        UsuarioEntity usuario = UsuarioEntity.builder()
            .id(usuarioId)
            .nombres("Sergio")
            .apellidos("Gómez")
            .correo("rollback." + usuarioId + "@test.com")
            .tipoDocumento(TipoDocumento.CC)
            .numeroDocumento(
                usuarioId.toString()
                    .replace("-", "")
                    .substring(0, 20)
            )
            .salarioBase(new BigDecimal("8000000.00"))
            .fechaCreacion(Instant.now())
            .build();

        usuarioJpaRepository.save(usuario);

        TipoPrestamoEntity tipoPrestamo =
            TipoPrestamoEntity.builder()
                .id(tipoPrestamoId)
                .nombre("LIBRE_INVERSION_ROLLBACK")
                .tasaAnual(new BigDecimal("14.5000"))
                .validacionAutomatica(false)
                .activo(true)
                .fechaCreacion(Instant.now())
                .build();

        tipoPrestamoJpaRepository.save(tipoPrestamo);

        SolicitudPrestamoEntity solicitud =
            SolicitudPrestamoEntity.builder()
                .id(solicitudId)
                .usuarioId(usuarioId)
                .tipoPrestamoId(tipoPrestamoId)
                .monto(new BigDecimal("5000000.00"))
                .plazoMeses(24)
                .estado(EstadoSolicitud.PENDIENTE_REVISION)
                .fechaSolicitud(Instant.now())
                .build();

        solicitudPrestamoJpaRepository.save(solicitud);
    }

    @Test
    void debeHacerRollbackCuandoFallaGuardadoDeCuotas()
        throws Exception {

        doThrow(
            new RuntimeException(
                "Error simulado al guardar las cuotas"
            )
        ).when(cuotaPlanPagoRepositoryPort)
            .guardarTodas(any(UUID.class), anyList());

        String requestBody = """
            {
              "decision": "APROBAR",
              "observacion": "Solicitud aprobada manualmente."
            }
            """;

        mockMvc.perform(
                patch(
                    "/api/v1/solicitudes/{solicitudId}/evaluacion-manual",
                    solicitudId
                )
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andExpect(status().isInternalServerError());

        SolicitudPrestamoEntity solicitudDespuesDelError =
            solicitudPrestamoJpaRepository
                .findById(solicitudId)
                .orElseThrow();

        /*
         * La solicitud fue modificada a APROBADA dentro del servicio,
         * pero la transacción debe deshacer esa modificación.
         */
        assertEquals(
            EstadoSolicitud.PENDIENTE_REVISION,
            solicitudDespuesDelError.getEstado()
        );

        /*
         * El préstamo se intentó guardar antes de guardar las cuotas,
         * pero también debe desaparecer debido al rollback.
         */
        assertEquals(
            0,
            prestamoJpaRepository.count()
        );

        /*
         * No debe existir ninguna cuota persistida.
         */
        assertEquals(
            0,
            cuotaPlanPagoJpaRepository.count()
        );
    }
}
