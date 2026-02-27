package com.etecsa.web.rest;

import static com.etecsa.domain.AlarmaAsserts.*;
import static com.etecsa.web.rest.TestUtil.createUpdateProxyForBean;
import static com.etecsa.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.etecsa.IntegrationTest;
import com.etecsa.domain.Alarma;
import com.etecsa.domain.EventoEquipo;
import com.etecsa.domain.User;
import com.etecsa.domain.enumeration.EstadoAlarma;
import com.etecsa.domain.enumeration.Severidad;
import com.etecsa.repository.AlarmaRepository;
import com.etecsa.repository.UserRepository;
import com.etecsa.service.AlarmaService;
import com.etecsa.service.dto.AlarmaDTO;
import com.etecsa.service.mapper.AlarmaMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AlarmaResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AlarmaResourceIT {

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_ACTIVATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_ACTIVATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_ACTIVATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final ZonedDateTime DEFAULT_DEACTIVATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_DEACTIVATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_DEACTIVATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final Severidad DEFAULT_SEVERIDAD = Severidad.BAJA;
    private static final Severidad UPDATED_SEVERIDAD = Severidad.MEDIA;

    private static final EstadoAlarma DEFAULT_ESTADO = EstadoAlarma.ACTIVA;
    private static final EstadoAlarma UPDATED_ESTADO = EstadoAlarma.RECONOCIDA;

    private static final String DEFAULT_MENSAJE_USUARIO = "AAAAAAAAAA";
    private static final String UPDATED_MENSAJE_USUARIO = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/alarmas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AlarmaRepository alarmaRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private AlarmaRepository alarmaRepositoryMock;

    @Autowired
    private AlarmaMapper alarmaMapper;

    @Mock
    private AlarmaService alarmaServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlarmaMockMvc;

    private Alarma alarma;

    private Alarma insertedAlarma;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Alarma createEntity() {
        return new Alarma()
            .descripcion(DEFAULT_DESCRIPCION)
            .activatedAt(DEFAULT_ACTIVATED_AT)
            .deactivatedAt(DEFAULT_DEACTIVATED_AT)
            .severidad(DEFAULT_SEVERIDAD)
            .estado(DEFAULT_ESTADO)
            .mensajeUsuario(DEFAULT_MENSAJE_USUARIO);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Alarma createUpdatedEntity() {
        return new Alarma()
            .descripcion(UPDATED_DESCRIPCION)
            .activatedAt(UPDATED_ACTIVATED_AT)
            .deactivatedAt(UPDATED_DEACTIVATED_AT)
            .severidad(UPDATED_SEVERIDAD)
            .estado(UPDATED_ESTADO)
            .mensajeUsuario(UPDATED_MENSAJE_USUARIO);
    }

    @BeforeEach
    void initTest() {
        alarma = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAlarma != null) {
            alarmaRepository.delete(insertedAlarma);
            insertedAlarma = null;
        }
    }

    @Test
    @Transactional
    void createAlarma() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Alarma
        AlarmaDTO alarmaDTO = alarmaMapper.toDto(alarma);
        var returnedAlarmaDTO = om.readValue(
            restAlarmaMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alarmaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AlarmaDTO.class
        );

        // Validate the Alarma in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAlarma = alarmaMapper.toEntity(returnedAlarmaDTO);
        assertAlarmaUpdatableFieldsEquals(returnedAlarma, getPersistedAlarma(returnedAlarma));

        insertedAlarma = returnedAlarma;
    }

    @Test
    @Transactional
    void createAlarmaWithExistingId() throws Exception {
        // Create the Alarma with an existing ID
        alarma.setId(1L);
        AlarmaDTO alarmaDTO = alarmaMapper.toDto(alarma);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlarmaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alarmaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Alarma in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkDescripcionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alarma.setDescripcion(null);

        // Create the Alarma, which fails.
        AlarmaDTO alarmaDTO = alarmaMapper.toDto(alarma);

        restAlarmaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alarmaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActivatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alarma.setActivatedAt(null);

        // Create the Alarma, which fails.
        AlarmaDTO alarmaDTO = alarmaMapper.toDto(alarma);

        restAlarmaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alarmaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSeveridadIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alarma.setSeveridad(null);

        // Create the Alarma, which fails.
        AlarmaDTO alarmaDTO = alarmaMapper.toDto(alarma);

        restAlarmaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alarmaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEstadoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        alarma.setEstado(null);

        // Create the Alarma, which fails.
        AlarmaDTO alarmaDTO = alarmaMapper.toDto(alarma);

        restAlarmaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alarmaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAlarmas() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList
        restAlarmaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alarma.getId().intValue())))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].activatedAt").value(hasItem(sameInstant(DEFAULT_ACTIVATED_AT))))
            .andExpect(jsonPath("$.[*].deactivatedAt").value(hasItem(sameInstant(DEFAULT_DEACTIVATED_AT))))
            .andExpect(jsonPath("$.[*].severidad").value(hasItem(DEFAULT_SEVERIDAD.toString())))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())))
            .andExpect(jsonPath("$.[*].mensajeUsuario").value(hasItem(DEFAULT_MENSAJE_USUARIO)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlarmasWithEagerRelationshipsIsEnabled() throws Exception {
        when(alarmaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlarmaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(alarmaServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlarmasWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(alarmaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlarmaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(alarmaRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getAlarma() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get the alarma
        restAlarmaMockMvc
            .perform(get(ENTITY_API_URL_ID, alarma.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(alarma.getId().intValue()))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION))
            .andExpect(jsonPath("$.activatedAt").value(sameInstant(DEFAULT_ACTIVATED_AT)))
            .andExpect(jsonPath("$.deactivatedAt").value(sameInstant(DEFAULT_DEACTIVATED_AT)))
            .andExpect(jsonPath("$.severidad").value(DEFAULT_SEVERIDAD.toString()))
            .andExpect(jsonPath("$.estado").value(DEFAULT_ESTADO.toString()))
            .andExpect(jsonPath("$.mensajeUsuario").value(DEFAULT_MENSAJE_USUARIO));
    }

    @Test
    @Transactional
    void getAlarmasByIdFiltering() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        Long id = alarma.getId();

        defaultAlarmaFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultAlarmaFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultAlarmaFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllAlarmasByDescripcionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where descripcion equals to
        defaultAlarmaFiltering("descripcion.equals=" + DEFAULT_DESCRIPCION, "descripcion.equals=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllAlarmasByDescripcionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where descripcion in
        defaultAlarmaFiltering(
            "descripcion.in=" + DEFAULT_DESCRIPCION + "," + UPDATED_DESCRIPCION,
            "descripcion.in=" + UPDATED_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllAlarmasByDescripcionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where descripcion is not null
        defaultAlarmaFiltering("descripcion.specified=true", "descripcion.specified=false");
    }

    @Test
    @Transactional
    void getAllAlarmasByDescripcionContainsSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where descripcion contains
        defaultAlarmaFiltering("descripcion.contains=" + DEFAULT_DESCRIPCION, "descripcion.contains=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllAlarmasByDescripcionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where descripcion does not contain
        defaultAlarmaFiltering("descripcion.doesNotContain=" + UPDATED_DESCRIPCION, "descripcion.doesNotContain=" + DEFAULT_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllAlarmasByActivatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where activatedAt equals to
        defaultAlarmaFiltering("activatedAt.equals=" + DEFAULT_ACTIVATED_AT, "activatedAt.equals=" + UPDATED_ACTIVATED_AT);
    }

    @Test
    @Transactional
    void getAllAlarmasByActivatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where activatedAt in
        defaultAlarmaFiltering(
            "activatedAt.in=" + DEFAULT_ACTIVATED_AT + "," + UPDATED_ACTIVATED_AT,
            "activatedAt.in=" + UPDATED_ACTIVATED_AT
        );
    }

    @Test
    @Transactional
    void getAllAlarmasByActivatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where activatedAt is not null
        defaultAlarmaFiltering("activatedAt.specified=true", "activatedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllAlarmasByActivatedAtIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where activatedAt is greater than or equal to
        defaultAlarmaFiltering(
            "activatedAt.greaterThanOrEqual=" + DEFAULT_ACTIVATED_AT,
            "activatedAt.greaterThanOrEqual=" + UPDATED_ACTIVATED_AT
        );
    }

    @Test
    @Transactional
    void getAllAlarmasByActivatedAtIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where activatedAt is less than or equal to
        defaultAlarmaFiltering(
            "activatedAt.lessThanOrEqual=" + DEFAULT_ACTIVATED_AT,
            "activatedAt.lessThanOrEqual=" + SMALLER_ACTIVATED_AT
        );
    }

    @Test
    @Transactional
    void getAllAlarmasByActivatedAtIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where activatedAt is less than
        defaultAlarmaFiltering("activatedAt.lessThan=" + UPDATED_ACTIVATED_AT, "activatedAt.lessThan=" + DEFAULT_ACTIVATED_AT);
    }

    @Test
    @Transactional
    void getAllAlarmasByActivatedAtIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where activatedAt is greater than
        defaultAlarmaFiltering("activatedAt.greaterThan=" + SMALLER_ACTIVATED_AT, "activatedAt.greaterThan=" + DEFAULT_ACTIVATED_AT);
    }

    @Test
    @Transactional
    void getAllAlarmasByDeactivatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where deactivatedAt equals to
        defaultAlarmaFiltering("deactivatedAt.equals=" + DEFAULT_DEACTIVATED_AT, "deactivatedAt.equals=" + UPDATED_DEACTIVATED_AT);
    }

    @Test
    @Transactional
    void getAllAlarmasByDeactivatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where deactivatedAt in
        defaultAlarmaFiltering(
            "deactivatedAt.in=" + DEFAULT_DEACTIVATED_AT + "," + UPDATED_DEACTIVATED_AT,
            "deactivatedAt.in=" + UPDATED_DEACTIVATED_AT
        );
    }

    @Test
    @Transactional
    void getAllAlarmasByDeactivatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where deactivatedAt is not null
        defaultAlarmaFiltering("deactivatedAt.specified=true", "deactivatedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllAlarmasByDeactivatedAtIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where deactivatedAt is greater than or equal to
        defaultAlarmaFiltering(
            "deactivatedAt.greaterThanOrEqual=" + DEFAULT_DEACTIVATED_AT,
            "deactivatedAt.greaterThanOrEqual=" + UPDATED_DEACTIVATED_AT
        );
    }

    @Test
    @Transactional
    void getAllAlarmasByDeactivatedAtIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where deactivatedAt is less than or equal to
        defaultAlarmaFiltering(
            "deactivatedAt.lessThanOrEqual=" + DEFAULT_DEACTIVATED_AT,
            "deactivatedAt.lessThanOrEqual=" + SMALLER_DEACTIVATED_AT
        );
    }

    @Test
    @Transactional
    void getAllAlarmasByDeactivatedAtIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where deactivatedAt is less than
        defaultAlarmaFiltering("deactivatedAt.lessThan=" + UPDATED_DEACTIVATED_AT, "deactivatedAt.lessThan=" + DEFAULT_DEACTIVATED_AT);
    }

    @Test
    @Transactional
    void getAllAlarmasByDeactivatedAtIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where deactivatedAt is greater than
        defaultAlarmaFiltering(
            "deactivatedAt.greaterThan=" + SMALLER_DEACTIVATED_AT,
            "deactivatedAt.greaterThan=" + DEFAULT_DEACTIVATED_AT
        );
    }

    @Test
    @Transactional
    void getAllAlarmasBySeveridadIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where severidad equals to
        defaultAlarmaFiltering("severidad.equals=" + DEFAULT_SEVERIDAD, "severidad.equals=" + UPDATED_SEVERIDAD);
    }

    @Test
    @Transactional
    void getAllAlarmasBySeveridadIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where severidad in
        defaultAlarmaFiltering("severidad.in=" + DEFAULT_SEVERIDAD + "," + UPDATED_SEVERIDAD, "severidad.in=" + UPDATED_SEVERIDAD);
    }

    @Test
    @Transactional
    void getAllAlarmasBySeveridadIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where severidad is not null
        defaultAlarmaFiltering("severidad.specified=true", "severidad.specified=false");
    }

    @Test
    @Transactional
    void getAllAlarmasByEstadoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where estado equals to
        defaultAlarmaFiltering("estado.equals=" + DEFAULT_ESTADO, "estado.equals=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllAlarmasByEstadoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where estado in
        defaultAlarmaFiltering("estado.in=" + DEFAULT_ESTADO + "," + UPDATED_ESTADO, "estado.in=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllAlarmasByEstadoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where estado is not null
        defaultAlarmaFiltering("estado.specified=true", "estado.specified=false");
    }

    @Test
    @Transactional
    void getAllAlarmasByMensajeUsuarioIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where mensajeUsuario equals to
        defaultAlarmaFiltering("mensajeUsuario.equals=" + DEFAULT_MENSAJE_USUARIO, "mensajeUsuario.equals=" + UPDATED_MENSAJE_USUARIO);
    }

    @Test
    @Transactional
    void getAllAlarmasByMensajeUsuarioIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where mensajeUsuario in
        defaultAlarmaFiltering(
            "mensajeUsuario.in=" + DEFAULT_MENSAJE_USUARIO + "," + UPDATED_MENSAJE_USUARIO,
            "mensajeUsuario.in=" + UPDATED_MENSAJE_USUARIO
        );
    }

    @Test
    @Transactional
    void getAllAlarmasByMensajeUsuarioIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where mensajeUsuario is not null
        defaultAlarmaFiltering("mensajeUsuario.specified=true", "mensajeUsuario.specified=false");
    }

    @Test
    @Transactional
    void getAllAlarmasByMensajeUsuarioContainsSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where mensajeUsuario contains
        defaultAlarmaFiltering("mensajeUsuario.contains=" + DEFAULT_MENSAJE_USUARIO, "mensajeUsuario.contains=" + UPDATED_MENSAJE_USUARIO);
    }

    @Test
    @Transactional
    void getAllAlarmasByMensajeUsuarioNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        // Get all the alarmaList where mensajeUsuario does not contain
        defaultAlarmaFiltering(
            "mensajeUsuario.doesNotContain=" + UPDATED_MENSAJE_USUARIO,
            "mensajeUsuario.doesNotContain=" + DEFAULT_MENSAJE_USUARIO
        );
    }

    @Test
    @Transactional
    void getAllAlarmasByEventoIsEqualToSomething() throws Exception {
        EventoEquipo evento;
        if (TestUtil.findAll(em, EventoEquipo.class).isEmpty()) {
            alarmaRepository.saveAndFlush(alarma);
            evento = EventoEquipoResourceIT.createEntity();
        } else {
            evento = TestUtil.findAll(em, EventoEquipo.class).get(0);
        }
        em.persist(evento);
        em.flush();
        alarma.setEvento(evento);
        alarmaRepository.saveAndFlush(alarma);
        Long eventoId = evento.getId();
        // Get all the alarmaList where evento equals to eventoId
        defaultAlarmaShouldBeFound("eventoId.equals=" + eventoId);

        // Get all the alarmaList where evento equals to (eventoId + 1)
        defaultAlarmaShouldNotBeFound("eventoId.equals=" + (eventoId + 1));
    }

    @Test
    @Transactional
    void getAllAlarmasByAcknowledgedByIsEqualToSomething() throws Exception {
        User acknowledgedBy;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            alarmaRepository.saveAndFlush(alarma);
            acknowledgedBy = UserResourceIT.createEntity();
        } else {
            acknowledgedBy = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(acknowledgedBy);
        em.flush();
        alarma.setAcknowledgedBy(acknowledgedBy);
        alarmaRepository.saveAndFlush(alarma);
        Long acknowledgedById = acknowledgedBy.getId();
        // Get all the alarmaList where acknowledgedBy equals to acknowledgedById
        defaultAlarmaShouldBeFound("acknowledgedById.equals=" + acknowledgedById);

        // Get all the alarmaList where acknowledgedBy equals to (acknowledgedById + 1)
        defaultAlarmaShouldNotBeFound("acknowledgedById.equals=" + (acknowledgedById + 1));
    }

    private void defaultAlarmaFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultAlarmaShouldBeFound(shouldBeFound);
        defaultAlarmaShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAlarmaShouldBeFound(String filter) throws Exception {
        restAlarmaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(alarma.getId().intValue())))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].activatedAt").value(hasItem(sameInstant(DEFAULT_ACTIVATED_AT))))
            .andExpect(jsonPath("$.[*].deactivatedAt").value(hasItem(sameInstant(DEFAULT_DEACTIVATED_AT))))
            .andExpect(jsonPath("$.[*].severidad").value(hasItem(DEFAULT_SEVERIDAD.toString())))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())))
            .andExpect(jsonPath("$.[*].mensajeUsuario").value(hasItem(DEFAULT_MENSAJE_USUARIO)));

        // Check, that the count call also returns 1
        restAlarmaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAlarmaShouldNotBeFound(String filter) throws Exception {
        restAlarmaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAlarmaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAlarma() throws Exception {
        // Get the alarma
        restAlarmaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAlarma() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alarma
        Alarma updatedAlarma = alarmaRepository.findById(alarma.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAlarma are not directly saved in db
        em.detach(updatedAlarma);
        updatedAlarma
            .descripcion(UPDATED_DESCRIPCION)
            .activatedAt(UPDATED_ACTIVATED_AT)
            .deactivatedAt(UPDATED_DEACTIVATED_AT)
            .severidad(UPDATED_SEVERIDAD)
            .estado(UPDATED_ESTADO)
            .mensajeUsuario(UPDATED_MENSAJE_USUARIO);
        AlarmaDTO alarmaDTO = alarmaMapper.toDto(updatedAlarma);

        restAlarmaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alarmaDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alarmaDTO))
            )
            .andExpect(status().isOk());

        // Validate the Alarma in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAlarmaToMatchAllProperties(updatedAlarma);
    }

    @Test
    @Transactional
    void putNonExistingAlarma() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alarma.setId(longCount.incrementAndGet());

        // Create the Alarma
        AlarmaDTO alarmaDTO = alarmaMapper.toDto(alarma);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlarmaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, alarmaDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alarmaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Alarma in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAlarma() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alarma.setId(longCount.incrementAndGet());

        // Create the Alarma
        AlarmaDTO alarmaDTO = alarmaMapper.toDto(alarma);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(alarmaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Alarma in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAlarma() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alarma.setId(longCount.incrementAndGet());

        // Create the Alarma
        AlarmaDTO alarmaDTO = alarmaMapper.toDto(alarma);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(alarmaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Alarma in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAlarmaWithPatch() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alarma using partial update
        Alarma partialUpdatedAlarma = new Alarma();
        partialUpdatedAlarma.setId(alarma.getId());

        partialUpdatedAlarma.estado(UPDATED_ESTADO).mensajeUsuario(UPDATED_MENSAJE_USUARIO);

        restAlarmaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlarma.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlarma))
            )
            .andExpect(status().isOk());

        // Validate the Alarma in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlarmaUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedAlarma, alarma), getPersistedAlarma(alarma));
    }

    @Test
    @Transactional
    void fullUpdateAlarmaWithPatch() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the alarma using partial update
        Alarma partialUpdatedAlarma = new Alarma();
        partialUpdatedAlarma.setId(alarma.getId());

        partialUpdatedAlarma
            .descripcion(UPDATED_DESCRIPCION)
            .activatedAt(UPDATED_ACTIVATED_AT)
            .deactivatedAt(UPDATED_DEACTIVATED_AT)
            .severidad(UPDATED_SEVERIDAD)
            .estado(UPDATED_ESTADO)
            .mensajeUsuario(UPDATED_MENSAJE_USUARIO);

        restAlarmaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlarma.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlarma))
            )
            .andExpect(status().isOk());

        // Validate the Alarma in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlarmaUpdatableFieldsEquals(partialUpdatedAlarma, getPersistedAlarma(partialUpdatedAlarma));
    }

    @Test
    @Transactional
    void patchNonExistingAlarma() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alarma.setId(longCount.incrementAndGet());

        // Create the Alarma
        AlarmaDTO alarmaDTO = alarmaMapper.toDto(alarma);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlarmaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, alarmaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(alarmaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Alarma in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAlarma() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alarma.setId(longCount.incrementAndGet());

        // Create the Alarma
        AlarmaDTO alarmaDTO = alarmaMapper.toDto(alarma);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(alarmaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Alarma in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAlarma() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        alarma.setId(longCount.incrementAndGet());

        // Create the Alarma
        AlarmaDTO alarmaDTO = alarmaMapper.toDto(alarma);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlarmaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(alarmaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Alarma in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAlarma() throws Exception {
        // Initialize the database
        insertedAlarma = alarmaRepository.saveAndFlush(alarma);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the alarma
        restAlarmaMockMvc
            .perform(delete(ENTITY_API_URL_ID, alarma.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return alarmaRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Alarma getPersistedAlarma(Alarma alarma) {
        return alarmaRepository.findById(alarma.getId()).orElseThrow();
    }

    protected void assertPersistedAlarmaToMatchAllProperties(Alarma expectedAlarma) {
        assertAlarmaAllPropertiesEquals(expectedAlarma, getPersistedAlarma(expectedAlarma));
    }

    protected void assertPersistedAlarmaToMatchUpdatableProperties(Alarma expectedAlarma) {
        assertAlarmaAllUpdatablePropertiesEquals(expectedAlarma, getPersistedAlarma(expectedAlarma));
    }
}
