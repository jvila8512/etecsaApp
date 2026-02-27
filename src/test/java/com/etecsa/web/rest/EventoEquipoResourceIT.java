package com.etecsa.web.rest;

import static com.etecsa.domain.EventoEquipoAsserts.*;
import static com.etecsa.web.rest.TestUtil.createUpdateProxyForBean;
import static com.etecsa.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.etecsa.IntegrationTest;
import com.etecsa.domain.Equipo;
import com.etecsa.domain.EventoEquipo;
import com.etecsa.domain.EventoPlantilla;
import com.etecsa.domain.enumeration.TipoDato;
import com.etecsa.domain.enumeration.TipoRegistro;
import com.etecsa.repository.EventoEquipoRepository;
import com.etecsa.service.EventoEquipoService;
import com.etecsa.service.dto.EventoEquipoDTO;
import com.etecsa.service.mapper.EventoEquipoMapper;
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
 * Integration tests for the {@link EventoEquipoResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class EventoEquipoResourceIT {

    private static final String DEFAULT_NOMBRE_VARIABLE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE_VARIABLE = "BBBBBBBBBB";

    private static final Integer DEFAULT_DIRECCION_MODBUS = 1;
    private static final Integer UPDATED_DIRECCION_MODBUS = 2;
    private static final Integer SMALLER_DIRECCION_MODBUS = 1 - 1;

    private static final TipoRegistro DEFAULT_TIPO_REGISTRO = TipoRegistro.BIT_LOGICO_M;
    private static final TipoRegistro UPDATED_TIPO_REGISTRO = TipoRegistro.PALABRA_MW;

    private static final TipoDato DEFAULT_TIPO_DATO = TipoDato.BOOLEAN;
    private static final TipoDato UPDATED_TIPO_DATO = TipoDato.INT16;

    private static final Boolean DEFAULT_ES_ESCRIBIBLE = false;
    private static final Boolean UPDATED_ES_ESCRIBIBLE = true;

    private static final Double DEFAULT_VALOR_NUMERICO = 1D;
    private static final Double UPDATED_VALOR_NUMERICO = 2D;
    private static final Double SMALLER_VALOR_NUMERICO = 1D - 1D;

    private static final Boolean DEFAULT_VALOR_BOOLEANO = false;
    private static final Boolean UPDATED_VALOR_BOOLEANO = true;

    private static final ZonedDateTime DEFAULT_TIMESTAMP_ACTUALIZACION = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_TIMESTAMP_ACTUALIZACION = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_TIMESTAMP_ACTUALIZACION = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final Integer DEFAULT_INTERVALO_LECTURA = 1;
    private static final Integer UPDATED_INTERVALO_LECTURA = 2;
    private static final Integer SMALLER_INTERVALO_LECTURA = 1 - 1;

    private static final Double DEFAULT_UMBRAL_ALERTA = 1D;
    private static final Double UPDATED_UMBRAL_ALERTA = 2D;
    private static final Double SMALLER_UMBRAL_ALERTA = 1D - 1D;

    private static final String ENTITY_API_URL = "/api/evento-equipos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EventoEquipoRepository eventoEquipoRepository;

    @Mock
    private EventoEquipoRepository eventoEquipoRepositoryMock;

    @Autowired
    private EventoEquipoMapper eventoEquipoMapper;

    @Mock
    private EventoEquipoService eventoEquipoServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEventoEquipoMockMvc;

    private EventoEquipo eventoEquipo;

    private EventoEquipo insertedEventoEquipo;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventoEquipo createEntity() {
        return new EventoEquipo()
            .nombreVariable(DEFAULT_NOMBRE_VARIABLE)
            .direccionModbus(DEFAULT_DIRECCION_MODBUS)
            .tipoRegistro(DEFAULT_TIPO_REGISTRO)
            .tipoDato(DEFAULT_TIPO_DATO)
            .esEscribible(DEFAULT_ES_ESCRIBIBLE)
            .valorNumerico(DEFAULT_VALOR_NUMERICO)
            .valorBooleano(DEFAULT_VALOR_BOOLEANO)
            .timestampActualizacion(DEFAULT_TIMESTAMP_ACTUALIZACION)
            .intervaloLectura(DEFAULT_INTERVALO_LECTURA)
            .umbralAlerta(DEFAULT_UMBRAL_ALERTA);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventoEquipo createUpdatedEntity() {
        return new EventoEquipo()
            .nombreVariable(UPDATED_NOMBRE_VARIABLE)
            .direccionModbus(UPDATED_DIRECCION_MODBUS)
            .tipoRegistro(UPDATED_TIPO_REGISTRO)
            .tipoDato(UPDATED_TIPO_DATO)
            .esEscribible(UPDATED_ES_ESCRIBIBLE)
            .valorNumerico(UPDATED_VALOR_NUMERICO)
            .valorBooleano(UPDATED_VALOR_BOOLEANO)
            .timestampActualizacion(UPDATED_TIMESTAMP_ACTUALIZACION)
            .intervaloLectura(UPDATED_INTERVALO_LECTURA)
            .umbralAlerta(UPDATED_UMBRAL_ALERTA);
    }

    @BeforeEach
    void initTest() {
        eventoEquipo = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedEventoEquipo != null) {
            eventoEquipoRepository.delete(insertedEventoEquipo);
            insertedEventoEquipo = null;
        }
    }

    @Test
    @Transactional
    void createEventoEquipo() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the EventoEquipo
        EventoEquipoDTO eventoEquipoDTO = eventoEquipoMapper.toDto(eventoEquipo);
        var returnedEventoEquipoDTO = om.readValue(
            restEventoEquipoMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoEquipoDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EventoEquipoDTO.class
        );

        // Validate the EventoEquipo in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedEventoEquipo = eventoEquipoMapper.toEntity(returnedEventoEquipoDTO);
        assertEventoEquipoUpdatableFieldsEquals(returnedEventoEquipo, getPersistedEventoEquipo(returnedEventoEquipo));

        insertedEventoEquipo = returnedEventoEquipo;
    }

    @Test
    @Transactional
    void createEventoEquipoWithExistingId() throws Exception {
        // Create the EventoEquipo with an existing ID
        eventoEquipo.setId(1L);
        EventoEquipoDTO eventoEquipoDTO = eventoEquipoMapper.toDto(eventoEquipo);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventoEquipoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoEquipoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the EventoEquipo in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreVariableIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        eventoEquipo.setNombreVariable(null);

        // Create the EventoEquipo, which fails.
        EventoEquipoDTO eventoEquipoDTO = eventoEquipoMapper.toDto(eventoEquipo);

        restEventoEquipoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoEquipoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDireccionModbusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        eventoEquipo.setDireccionModbus(null);

        // Create the EventoEquipo, which fails.
        EventoEquipoDTO eventoEquipoDTO = eventoEquipoMapper.toDto(eventoEquipo);

        restEventoEquipoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoEquipoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTipoRegistroIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        eventoEquipo.setTipoRegistro(null);

        // Create the EventoEquipo, which fails.
        EventoEquipoDTO eventoEquipoDTO = eventoEquipoMapper.toDto(eventoEquipo);

        restEventoEquipoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoEquipoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTipoDatoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        eventoEquipo.setTipoDato(null);

        // Create the EventoEquipo, which fails.
        EventoEquipoDTO eventoEquipoDTO = eventoEquipoMapper.toDto(eventoEquipo);

        restEventoEquipoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoEquipoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllEventoEquipos() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList
        restEventoEquipoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventoEquipo.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombreVariable").value(hasItem(DEFAULT_NOMBRE_VARIABLE)))
            .andExpect(jsonPath("$.[*].direccionModbus").value(hasItem(DEFAULT_DIRECCION_MODBUS)))
            .andExpect(jsonPath("$.[*].tipoRegistro").value(hasItem(DEFAULT_TIPO_REGISTRO.toString())))
            .andExpect(jsonPath("$.[*].tipoDato").value(hasItem(DEFAULT_TIPO_DATO.toString())))
            .andExpect(jsonPath("$.[*].esEscribible").value(hasItem(DEFAULT_ES_ESCRIBIBLE)))
            .andExpect(jsonPath("$.[*].valorNumerico").value(hasItem(DEFAULT_VALOR_NUMERICO)))
            .andExpect(jsonPath("$.[*].valorBooleano").value(hasItem(DEFAULT_VALOR_BOOLEANO)))
            .andExpect(jsonPath("$.[*].timestampActualizacion").value(hasItem(sameInstant(DEFAULT_TIMESTAMP_ACTUALIZACION))))
            .andExpect(jsonPath("$.[*].intervaloLectura").value(hasItem(DEFAULT_INTERVALO_LECTURA)))
            .andExpect(jsonPath("$.[*].umbralAlerta").value(hasItem(DEFAULT_UMBRAL_ALERTA)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEventoEquiposWithEagerRelationshipsIsEnabled() throws Exception {
        when(eventoEquipoServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restEventoEquipoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(eventoEquipoServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEventoEquiposWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(eventoEquipoServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restEventoEquipoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(eventoEquipoRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getEventoEquipo() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get the eventoEquipo
        restEventoEquipoMockMvc
            .perform(get(ENTITY_API_URL_ID, eventoEquipo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(eventoEquipo.getId().intValue()))
            .andExpect(jsonPath("$.nombreVariable").value(DEFAULT_NOMBRE_VARIABLE))
            .andExpect(jsonPath("$.direccionModbus").value(DEFAULT_DIRECCION_MODBUS))
            .andExpect(jsonPath("$.tipoRegistro").value(DEFAULT_TIPO_REGISTRO.toString()))
            .andExpect(jsonPath("$.tipoDato").value(DEFAULT_TIPO_DATO.toString()))
            .andExpect(jsonPath("$.esEscribible").value(DEFAULT_ES_ESCRIBIBLE))
            .andExpect(jsonPath("$.valorNumerico").value(DEFAULT_VALOR_NUMERICO))
            .andExpect(jsonPath("$.valorBooleano").value(DEFAULT_VALOR_BOOLEANO))
            .andExpect(jsonPath("$.timestampActualizacion").value(sameInstant(DEFAULT_TIMESTAMP_ACTUALIZACION)))
            .andExpect(jsonPath("$.intervaloLectura").value(DEFAULT_INTERVALO_LECTURA))
            .andExpect(jsonPath("$.umbralAlerta").value(DEFAULT_UMBRAL_ALERTA));
    }

    @Test
    @Transactional
    void getEventoEquiposByIdFiltering() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        Long id = eventoEquipo.getId();

        defaultEventoEquipoFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultEventoEquipoFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultEventoEquipoFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllEventoEquiposByNombreVariableIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where nombreVariable equals to
        defaultEventoEquipoFiltering(
            "nombreVariable.equals=" + DEFAULT_NOMBRE_VARIABLE,
            "nombreVariable.equals=" + UPDATED_NOMBRE_VARIABLE
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByNombreVariableIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where nombreVariable in
        defaultEventoEquipoFiltering(
            "nombreVariable.in=" + DEFAULT_NOMBRE_VARIABLE + "," + UPDATED_NOMBRE_VARIABLE,
            "nombreVariable.in=" + UPDATED_NOMBRE_VARIABLE
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByNombreVariableIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where nombreVariable is not null
        defaultEventoEquipoFiltering("nombreVariable.specified=true", "nombreVariable.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoEquiposByNombreVariableContainsSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where nombreVariable contains
        defaultEventoEquipoFiltering(
            "nombreVariable.contains=" + DEFAULT_NOMBRE_VARIABLE,
            "nombreVariable.contains=" + UPDATED_NOMBRE_VARIABLE
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByNombreVariableNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where nombreVariable does not contain
        defaultEventoEquipoFiltering(
            "nombreVariable.doesNotContain=" + UPDATED_NOMBRE_VARIABLE,
            "nombreVariable.doesNotContain=" + DEFAULT_NOMBRE_VARIABLE
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByDireccionModbusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where direccionModbus equals to
        defaultEventoEquipoFiltering(
            "direccionModbus.equals=" + DEFAULT_DIRECCION_MODBUS,
            "direccionModbus.equals=" + UPDATED_DIRECCION_MODBUS
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByDireccionModbusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where direccionModbus in
        defaultEventoEquipoFiltering(
            "direccionModbus.in=" + DEFAULT_DIRECCION_MODBUS + "," + UPDATED_DIRECCION_MODBUS,
            "direccionModbus.in=" + UPDATED_DIRECCION_MODBUS
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByDireccionModbusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where direccionModbus is not null
        defaultEventoEquipoFiltering("direccionModbus.specified=true", "direccionModbus.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoEquiposByDireccionModbusIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where direccionModbus is greater than or equal to
        defaultEventoEquipoFiltering(
            "direccionModbus.greaterThanOrEqual=" + DEFAULT_DIRECCION_MODBUS,
            "direccionModbus.greaterThanOrEqual=" + UPDATED_DIRECCION_MODBUS
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByDireccionModbusIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where direccionModbus is less than or equal to
        defaultEventoEquipoFiltering(
            "direccionModbus.lessThanOrEqual=" + DEFAULT_DIRECCION_MODBUS,
            "direccionModbus.lessThanOrEqual=" + SMALLER_DIRECCION_MODBUS
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByDireccionModbusIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where direccionModbus is less than
        defaultEventoEquipoFiltering(
            "direccionModbus.lessThan=" + UPDATED_DIRECCION_MODBUS,
            "direccionModbus.lessThan=" + DEFAULT_DIRECCION_MODBUS
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByDireccionModbusIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where direccionModbus is greater than
        defaultEventoEquipoFiltering(
            "direccionModbus.greaterThan=" + SMALLER_DIRECCION_MODBUS,
            "direccionModbus.greaterThan=" + DEFAULT_DIRECCION_MODBUS
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByTipoRegistroIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where tipoRegistro equals to
        defaultEventoEquipoFiltering("tipoRegistro.equals=" + DEFAULT_TIPO_REGISTRO, "tipoRegistro.equals=" + UPDATED_TIPO_REGISTRO);
    }

    @Test
    @Transactional
    void getAllEventoEquiposByTipoRegistroIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where tipoRegistro in
        defaultEventoEquipoFiltering(
            "tipoRegistro.in=" + DEFAULT_TIPO_REGISTRO + "," + UPDATED_TIPO_REGISTRO,
            "tipoRegistro.in=" + UPDATED_TIPO_REGISTRO
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByTipoRegistroIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where tipoRegistro is not null
        defaultEventoEquipoFiltering("tipoRegistro.specified=true", "tipoRegistro.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoEquiposByTipoDatoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where tipoDato equals to
        defaultEventoEquipoFiltering("tipoDato.equals=" + DEFAULT_TIPO_DATO, "tipoDato.equals=" + UPDATED_TIPO_DATO);
    }

    @Test
    @Transactional
    void getAllEventoEquiposByTipoDatoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where tipoDato in
        defaultEventoEquipoFiltering("tipoDato.in=" + DEFAULT_TIPO_DATO + "," + UPDATED_TIPO_DATO, "tipoDato.in=" + UPDATED_TIPO_DATO);
    }

    @Test
    @Transactional
    void getAllEventoEquiposByTipoDatoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where tipoDato is not null
        defaultEventoEquipoFiltering("tipoDato.specified=true", "tipoDato.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoEquiposByEsEscribibleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where esEscribible equals to
        defaultEventoEquipoFiltering("esEscribible.equals=" + DEFAULT_ES_ESCRIBIBLE, "esEscribible.equals=" + UPDATED_ES_ESCRIBIBLE);
    }

    @Test
    @Transactional
    void getAllEventoEquiposByEsEscribibleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where esEscribible in
        defaultEventoEquipoFiltering(
            "esEscribible.in=" + DEFAULT_ES_ESCRIBIBLE + "," + UPDATED_ES_ESCRIBIBLE,
            "esEscribible.in=" + UPDATED_ES_ESCRIBIBLE
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByEsEscribibleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where esEscribible is not null
        defaultEventoEquipoFiltering("esEscribible.specified=true", "esEscribible.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoEquiposByValorNumericoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where valorNumerico equals to
        defaultEventoEquipoFiltering("valorNumerico.equals=" + DEFAULT_VALOR_NUMERICO, "valorNumerico.equals=" + UPDATED_VALOR_NUMERICO);
    }

    @Test
    @Transactional
    void getAllEventoEquiposByValorNumericoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where valorNumerico in
        defaultEventoEquipoFiltering(
            "valorNumerico.in=" + DEFAULT_VALOR_NUMERICO + "," + UPDATED_VALOR_NUMERICO,
            "valorNumerico.in=" + UPDATED_VALOR_NUMERICO
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByValorNumericoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where valorNumerico is not null
        defaultEventoEquipoFiltering("valorNumerico.specified=true", "valorNumerico.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoEquiposByValorNumericoIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where valorNumerico is greater than or equal to
        defaultEventoEquipoFiltering(
            "valorNumerico.greaterThanOrEqual=" + DEFAULT_VALOR_NUMERICO,
            "valorNumerico.greaterThanOrEqual=" + UPDATED_VALOR_NUMERICO
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByValorNumericoIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where valorNumerico is less than or equal to
        defaultEventoEquipoFiltering(
            "valorNumerico.lessThanOrEqual=" + DEFAULT_VALOR_NUMERICO,
            "valorNumerico.lessThanOrEqual=" + SMALLER_VALOR_NUMERICO
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByValorNumericoIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where valorNumerico is less than
        defaultEventoEquipoFiltering(
            "valorNumerico.lessThan=" + UPDATED_VALOR_NUMERICO,
            "valorNumerico.lessThan=" + DEFAULT_VALOR_NUMERICO
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByValorNumericoIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where valorNumerico is greater than
        defaultEventoEquipoFiltering(
            "valorNumerico.greaterThan=" + SMALLER_VALOR_NUMERICO,
            "valorNumerico.greaterThan=" + DEFAULT_VALOR_NUMERICO
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByValorBooleanoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where valorBooleano equals to
        defaultEventoEquipoFiltering("valorBooleano.equals=" + DEFAULT_VALOR_BOOLEANO, "valorBooleano.equals=" + UPDATED_VALOR_BOOLEANO);
    }

    @Test
    @Transactional
    void getAllEventoEquiposByValorBooleanoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where valorBooleano in
        defaultEventoEquipoFiltering(
            "valorBooleano.in=" + DEFAULT_VALOR_BOOLEANO + "," + UPDATED_VALOR_BOOLEANO,
            "valorBooleano.in=" + UPDATED_VALOR_BOOLEANO
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByValorBooleanoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where valorBooleano is not null
        defaultEventoEquipoFiltering("valorBooleano.specified=true", "valorBooleano.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoEquiposByTimestampActualizacionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where timestampActualizacion equals to
        defaultEventoEquipoFiltering(
            "timestampActualizacion.equals=" + DEFAULT_TIMESTAMP_ACTUALIZACION,
            "timestampActualizacion.equals=" + UPDATED_TIMESTAMP_ACTUALIZACION
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByTimestampActualizacionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where timestampActualizacion in
        defaultEventoEquipoFiltering(
            "timestampActualizacion.in=" + DEFAULT_TIMESTAMP_ACTUALIZACION + "," + UPDATED_TIMESTAMP_ACTUALIZACION,
            "timestampActualizacion.in=" + UPDATED_TIMESTAMP_ACTUALIZACION
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByTimestampActualizacionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where timestampActualizacion is not null
        defaultEventoEquipoFiltering("timestampActualizacion.specified=true", "timestampActualizacion.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoEquiposByTimestampActualizacionIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where timestampActualizacion is greater than or equal to
        defaultEventoEquipoFiltering(
            "timestampActualizacion.greaterThanOrEqual=" + DEFAULT_TIMESTAMP_ACTUALIZACION,
            "timestampActualizacion.greaterThanOrEqual=" + UPDATED_TIMESTAMP_ACTUALIZACION
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByTimestampActualizacionIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where timestampActualizacion is less than or equal to
        defaultEventoEquipoFiltering(
            "timestampActualizacion.lessThanOrEqual=" + DEFAULT_TIMESTAMP_ACTUALIZACION,
            "timestampActualizacion.lessThanOrEqual=" + SMALLER_TIMESTAMP_ACTUALIZACION
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByTimestampActualizacionIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where timestampActualizacion is less than
        defaultEventoEquipoFiltering(
            "timestampActualizacion.lessThan=" + UPDATED_TIMESTAMP_ACTUALIZACION,
            "timestampActualizacion.lessThan=" + DEFAULT_TIMESTAMP_ACTUALIZACION
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByTimestampActualizacionIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where timestampActualizacion is greater than
        defaultEventoEquipoFiltering(
            "timestampActualizacion.greaterThan=" + SMALLER_TIMESTAMP_ACTUALIZACION,
            "timestampActualizacion.greaterThan=" + DEFAULT_TIMESTAMP_ACTUALIZACION
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByIntervaloLecturaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where intervaloLectura equals to
        defaultEventoEquipoFiltering(
            "intervaloLectura.equals=" + DEFAULT_INTERVALO_LECTURA,
            "intervaloLectura.equals=" + UPDATED_INTERVALO_LECTURA
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByIntervaloLecturaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where intervaloLectura in
        defaultEventoEquipoFiltering(
            "intervaloLectura.in=" + DEFAULT_INTERVALO_LECTURA + "," + UPDATED_INTERVALO_LECTURA,
            "intervaloLectura.in=" + UPDATED_INTERVALO_LECTURA
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByIntervaloLecturaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where intervaloLectura is not null
        defaultEventoEquipoFiltering("intervaloLectura.specified=true", "intervaloLectura.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoEquiposByIntervaloLecturaIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where intervaloLectura is greater than or equal to
        defaultEventoEquipoFiltering(
            "intervaloLectura.greaterThanOrEqual=" + DEFAULT_INTERVALO_LECTURA,
            "intervaloLectura.greaterThanOrEqual=" + UPDATED_INTERVALO_LECTURA
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByIntervaloLecturaIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where intervaloLectura is less than or equal to
        defaultEventoEquipoFiltering(
            "intervaloLectura.lessThanOrEqual=" + DEFAULT_INTERVALO_LECTURA,
            "intervaloLectura.lessThanOrEqual=" + SMALLER_INTERVALO_LECTURA
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByIntervaloLecturaIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where intervaloLectura is less than
        defaultEventoEquipoFiltering(
            "intervaloLectura.lessThan=" + UPDATED_INTERVALO_LECTURA,
            "intervaloLectura.lessThan=" + DEFAULT_INTERVALO_LECTURA
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByIntervaloLecturaIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where intervaloLectura is greater than
        defaultEventoEquipoFiltering(
            "intervaloLectura.greaterThan=" + SMALLER_INTERVALO_LECTURA,
            "intervaloLectura.greaterThan=" + DEFAULT_INTERVALO_LECTURA
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByUmbralAlertaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where umbralAlerta equals to
        defaultEventoEquipoFiltering("umbralAlerta.equals=" + DEFAULT_UMBRAL_ALERTA, "umbralAlerta.equals=" + UPDATED_UMBRAL_ALERTA);
    }

    @Test
    @Transactional
    void getAllEventoEquiposByUmbralAlertaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where umbralAlerta in
        defaultEventoEquipoFiltering(
            "umbralAlerta.in=" + DEFAULT_UMBRAL_ALERTA + "," + UPDATED_UMBRAL_ALERTA,
            "umbralAlerta.in=" + UPDATED_UMBRAL_ALERTA
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByUmbralAlertaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where umbralAlerta is not null
        defaultEventoEquipoFiltering("umbralAlerta.specified=true", "umbralAlerta.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoEquiposByUmbralAlertaIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where umbralAlerta is greater than or equal to
        defaultEventoEquipoFiltering(
            "umbralAlerta.greaterThanOrEqual=" + DEFAULT_UMBRAL_ALERTA,
            "umbralAlerta.greaterThanOrEqual=" + UPDATED_UMBRAL_ALERTA
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByUmbralAlertaIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where umbralAlerta is less than or equal to
        defaultEventoEquipoFiltering(
            "umbralAlerta.lessThanOrEqual=" + DEFAULT_UMBRAL_ALERTA,
            "umbralAlerta.lessThanOrEqual=" + SMALLER_UMBRAL_ALERTA
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByUmbralAlertaIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where umbralAlerta is less than
        defaultEventoEquipoFiltering("umbralAlerta.lessThan=" + UPDATED_UMBRAL_ALERTA, "umbralAlerta.lessThan=" + DEFAULT_UMBRAL_ALERTA);
    }

    @Test
    @Transactional
    void getAllEventoEquiposByUmbralAlertaIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        // Get all the eventoEquipoList where umbralAlerta is greater than
        defaultEventoEquipoFiltering(
            "umbralAlerta.greaterThan=" + SMALLER_UMBRAL_ALERTA,
            "umbralAlerta.greaterThan=" + DEFAULT_UMBRAL_ALERTA
        );
    }

    @Test
    @Transactional
    void getAllEventoEquiposByEquipoIsEqualToSomething() throws Exception {
        Equipo equipo;
        if (TestUtil.findAll(em, Equipo.class).isEmpty()) {
            eventoEquipoRepository.saveAndFlush(eventoEquipo);
            equipo = EquipoResourceIT.createEntity();
        } else {
            equipo = TestUtil.findAll(em, Equipo.class).get(0);
        }
        em.persist(equipo);
        em.flush();
        eventoEquipo.setEquipo(equipo);
        eventoEquipoRepository.saveAndFlush(eventoEquipo);
        Long equipoId = equipo.getId();
        // Get all the eventoEquipoList where equipo equals to equipoId
        defaultEventoEquipoShouldBeFound("equipoId.equals=" + equipoId);

        // Get all the eventoEquipoList where equipo equals to (equipoId + 1)
        defaultEventoEquipoShouldNotBeFound("equipoId.equals=" + (equipoId + 1));
    }

    @Test
    @Transactional
    void getAllEventoEquiposByPlantillaIsEqualToSomething() throws Exception {
        EventoPlantilla plantilla;
        if (TestUtil.findAll(em, EventoPlantilla.class).isEmpty()) {
            eventoEquipoRepository.saveAndFlush(eventoEquipo);
            plantilla = EventoPlantillaResourceIT.createEntity();
        } else {
            plantilla = TestUtil.findAll(em, EventoPlantilla.class).get(0);
        }
        em.persist(plantilla);
        em.flush();
        eventoEquipo.setPlantilla(plantilla);
        eventoEquipoRepository.saveAndFlush(eventoEquipo);
        Long plantillaId = plantilla.getId();
        // Get all the eventoEquipoList where plantilla equals to plantillaId
        defaultEventoEquipoShouldBeFound("plantillaId.equals=" + plantillaId);

        // Get all the eventoEquipoList where plantilla equals to (plantillaId + 1)
        defaultEventoEquipoShouldNotBeFound("plantillaId.equals=" + (plantillaId + 1));
    }

    private void defaultEventoEquipoFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultEventoEquipoShouldBeFound(shouldBeFound);
        defaultEventoEquipoShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultEventoEquipoShouldBeFound(String filter) throws Exception {
        restEventoEquipoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventoEquipo.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombreVariable").value(hasItem(DEFAULT_NOMBRE_VARIABLE)))
            .andExpect(jsonPath("$.[*].direccionModbus").value(hasItem(DEFAULT_DIRECCION_MODBUS)))
            .andExpect(jsonPath("$.[*].tipoRegistro").value(hasItem(DEFAULT_TIPO_REGISTRO.toString())))
            .andExpect(jsonPath("$.[*].tipoDato").value(hasItem(DEFAULT_TIPO_DATO.toString())))
            .andExpect(jsonPath("$.[*].esEscribible").value(hasItem(DEFAULT_ES_ESCRIBIBLE)))
            .andExpect(jsonPath("$.[*].valorNumerico").value(hasItem(DEFAULT_VALOR_NUMERICO)))
            .andExpect(jsonPath("$.[*].valorBooleano").value(hasItem(DEFAULT_VALOR_BOOLEANO)))
            .andExpect(jsonPath("$.[*].timestampActualizacion").value(hasItem(sameInstant(DEFAULT_TIMESTAMP_ACTUALIZACION))))
            .andExpect(jsonPath("$.[*].intervaloLectura").value(hasItem(DEFAULT_INTERVALO_LECTURA)))
            .andExpect(jsonPath("$.[*].umbralAlerta").value(hasItem(DEFAULT_UMBRAL_ALERTA)));

        // Check, that the count call also returns 1
        restEventoEquipoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultEventoEquipoShouldNotBeFound(String filter) throws Exception {
        restEventoEquipoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restEventoEquipoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingEventoEquipo() throws Exception {
        // Get the eventoEquipo
        restEventoEquipoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEventoEquipo() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventoEquipo
        EventoEquipo updatedEventoEquipo = eventoEquipoRepository.findById(eventoEquipo.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEventoEquipo are not directly saved in db
        em.detach(updatedEventoEquipo);
        updatedEventoEquipo
            .nombreVariable(UPDATED_NOMBRE_VARIABLE)
            .direccionModbus(UPDATED_DIRECCION_MODBUS)
            .tipoRegistro(UPDATED_TIPO_REGISTRO)
            .tipoDato(UPDATED_TIPO_DATO)
            .esEscribible(UPDATED_ES_ESCRIBIBLE)
            .valorNumerico(UPDATED_VALOR_NUMERICO)
            .valorBooleano(UPDATED_VALOR_BOOLEANO)
            .timestampActualizacion(UPDATED_TIMESTAMP_ACTUALIZACION)
            .intervaloLectura(UPDATED_INTERVALO_LECTURA)
            .umbralAlerta(UPDATED_UMBRAL_ALERTA);
        EventoEquipoDTO eventoEquipoDTO = eventoEquipoMapper.toDto(updatedEventoEquipo);

        restEventoEquipoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, eventoEquipoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(eventoEquipoDTO))
            )
            .andExpect(status().isOk());

        // Validate the EventoEquipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEventoEquipoToMatchAllProperties(updatedEventoEquipo);
    }

    @Test
    @Transactional
    void putNonExistingEventoEquipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoEquipo.setId(longCount.incrementAndGet());

        // Create the EventoEquipo
        EventoEquipoDTO eventoEquipoDTO = eventoEquipoMapper.toDto(eventoEquipo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEventoEquipoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, eventoEquipoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(eventoEquipoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventoEquipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEventoEquipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoEquipo.setId(longCount.incrementAndGet());

        // Create the EventoEquipo
        EventoEquipoDTO eventoEquipoDTO = eventoEquipoMapper.toDto(eventoEquipo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventoEquipoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(eventoEquipoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventoEquipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEventoEquipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoEquipo.setId(longCount.incrementAndGet());

        // Create the EventoEquipo
        EventoEquipoDTO eventoEquipoDTO = eventoEquipoMapper.toDto(eventoEquipo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventoEquipoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoEquipoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the EventoEquipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEventoEquipoWithPatch() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventoEquipo using partial update
        EventoEquipo partialUpdatedEventoEquipo = new EventoEquipo();
        partialUpdatedEventoEquipo.setId(eventoEquipo.getId());

        partialUpdatedEventoEquipo
            .esEscribible(UPDATED_ES_ESCRIBIBLE)
            .valorNumerico(UPDATED_VALOR_NUMERICO)
            .timestampActualizacion(UPDATED_TIMESTAMP_ACTUALIZACION);

        restEventoEquipoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventoEquipo.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEventoEquipo))
            )
            .andExpect(status().isOk());

        // Validate the EventoEquipo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEventoEquipoUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedEventoEquipo, eventoEquipo),
            getPersistedEventoEquipo(eventoEquipo)
        );
    }

    @Test
    @Transactional
    void fullUpdateEventoEquipoWithPatch() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventoEquipo using partial update
        EventoEquipo partialUpdatedEventoEquipo = new EventoEquipo();
        partialUpdatedEventoEquipo.setId(eventoEquipo.getId());

        partialUpdatedEventoEquipo
            .nombreVariable(UPDATED_NOMBRE_VARIABLE)
            .direccionModbus(UPDATED_DIRECCION_MODBUS)
            .tipoRegistro(UPDATED_TIPO_REGISTRO)
            .tipoDato(UPDATED_TIPO_DATO)
            .esEscribible(UPDATED_ES_ESCRIBIBLE)
            .valorNumerico(UPDATED_VALOR_NUMERICO)
            .valorBooleano(UPDATED_VALOR_BOOLEANO)
            .timestampActualizacion(UPDATED_TIMESTAMP_ACTUALIZACION)
            .intervaloLectura(UPDATED_INTERVALO_LECTURA)
            .umbralAlerta(UPDATED_UMBRAL_ALERTA);

        restEventoEquipoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventoEquipo.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEventoEquipo))
            )
            .andExpect(status().isOk());

        // Validate the EventoEquipo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEventoEquipoUpdatableFieldsEquals(partialUpdatedEventoEquipo, getPersistedEventoEquipo(partialUpdatedEventoEquipo));
    }

    @Test
    @Transactional
    void patchNonExistingEventoEquipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoEquipo.setId(longCount.incrementAndGet());

        // Create the EventoEquipo
        EventoEquipoDTO eventoEquipoDTO = eventoEquipoMapper.toDto(eventoEquipo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEventoEquipoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, eventoEquipoDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(eventoEquipoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventoEquipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEventoEquipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoEquipo.setId(longCount.incrementAndGet());

        // Create the EventoEquipo
        EventoEquipoDTO eventoEquipoDTO = eventoEquipoMapper.toDto(eventoEquipo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventoEquipoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(eventoEquipoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventoEquipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEventoEquipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoEquipo.setId(longCount.incrementAndGet());

        // Create the EventoEquipo
        EventoEquipoDTO eventoEquipoDTO = eventoEquipoMapper.toDto(eventoEquipo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventoEquipoMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(eventoEquipoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the EventoEquipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEventoEquipo() throws Exception {
        // Initialize the database
        insertedEventoEquipo = eventoEquipoRepository.saveAndFlush(eventoEquipo);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the eventoEquipo
        restEventoEquipoMockMvc
            .perform(delete(ENTITY_API_URL_ID, eventoEquipo.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return eventoEquipoRepository.count();
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

    protected EventoEquipo getPersistedEventoEquipo(EventoEquipo eventoEquipo) {
        return eventoEquipoRepository.findById(eventoEquipo.getId()).orElseThrow();
    }

    protected void assertPersistedEventoEquipoToMatchAllProperties(EventoEquipo expectedEventoEquipo) {
        assertEventoEquipoAllPropertiesEquals(expectedEventoEquipo, getPersistedEventoEquipo(expectedEventoEquipo));
    }

    protected void assertPersistedEventoEquipoToMatchUpdatableProperties(EventoEquipo expectedEventoEquipo) {
        assertEventoEquipoAllUpdatablePropertiesEquals(expectedEventoEquipo, getPersistedEventoEquipo(expectedEventoEquipo));
    }
}
