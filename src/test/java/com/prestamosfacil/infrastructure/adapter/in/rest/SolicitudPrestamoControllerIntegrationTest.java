package com.prestamosfacil.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class SolicitudPrestamoControllerIntegrationTest {

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
    private ObjectMapper objectMapper;

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
            .correo("sergio." + usuarioId + "@test.com")
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
                .nombre("LIBRE_INVERSION")
                .tasaAnual(new BigDecimal("14.5000"))
                .validacionAutomatica(true)
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
    void debeAprobarSolicitudMedianteEndpoint() throws Exception {
        String requestBody = """
            {
              "decision": "APROBAR",
              "observacion": "La solicitud cumple con los criterios definidos."
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
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.solicitudId")
                    .value(solicitudId.toString())
            )
            .andExpect(
                jsonPath("$.estado")
                    .value("APROBADA")
            )
            .andExpect(
                jsonPath("$.prestamoId")
                    .isNotEmpty()
            )
            .andExpect(
                jsonPath("$.mensaje")
                    .value(
                        "Solicitud aprobada y préstamo creado correctamente"
                    )
            );

        SolicitudPrestamoEntity solicitudActualizada =
            solicitudPrestamoJpaRepository
                .findById(solicitudId)
                .orElseThrow();

        assertEquals(
            EstadoSolicitud.APROBADA,
            solicitudActualizada.getEstado()
        );

        assertEquals(
            1,
            prestamoJpaRepository.count()
        );

        assertEquals(
            24,
            cuotaPlanPagoJpaRepository.count()
        );
    }
}
