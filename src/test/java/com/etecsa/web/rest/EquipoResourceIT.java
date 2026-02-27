package com.etecsa.web.rest;

import static com.etecsa.domain.EquipoAsserts.*;
import static com.etecsa.web.rest.TestUtil.createUpdateProxyForBean;
import static com.etecsa.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.etecsa.IntegrationTest;
import com.etecsa.domain.Equipo;
import com.etecsa.domain.Especialidad;
import com.etecsa.domain.Sitio;
import com.etecsa.domain.enumeration.EstadoEquipo;
import com.etecsa.repository.EquipoRepository;
import com.etecsa.service.EquipoService;
import com.etecsa.service.dto.EquipoDTO;
import com.etecsa.service.mapper.EquipoMapper;
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
 * Integration tests for the {@link EquipoResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class EquipoResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_DIRECCION_IP = "AAAAAAAAAA";
    private static final String UPDATED_DIRECCION_IP = "BBBBBBBBBB";

    private static final Integer DEFAULT_MODBUS_SLAVE_ID = 1;
    private static final Integer UPDATED_MODBUS_SLAVE_ID = 2;
    private static final Integer SMALLER_MODBUS_SLAVE_ID = 1 - 1;

    private static final String DEFAULT_MODELO = "AAAAAAAAAA";
    private static final String UPDATED_MODELO = "BBBBBBBBBB";

    private static final String DEFAULT_FIRMWARE_VERSION = "AAAAAAAAAA";
    private static final String UPDATED_FIRMWARE_VERSION = "BBBBBBBBBB";

    private static final EstadoEquipo DEFAULT_ESTADO = EstadoEquipo.OPERATIVO;
    private static final EstadoEquipo UPDATED_ESTADO = EstadoEquipo.MANTENIMIENTO;

    private static final ZonedDateTime DEFAULT_ULTIMO_HEARTBEAT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_ULTIMO_HEARTBEAT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_ULTIMO_HEARTBEAT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final String ENTITY_API_URL = "/api/equipos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EquipoRepository equipoRepository;

    @Mock
    private EquipoRepository equipoRepositoryMock;

    @Autowired
    private EquipoMapper equipoMapper;

    @Mock
    private EquipoService equipoServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEquipoMockMvc;

    private Equipo equipo;

    private Equipo insertedEquipo;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Equipo createEntity() {
        return new Equipo()
            .nombre(DEFAULT_NOMBRE)
            .direccionIp(DEFAULT_DIRECCION_IP)
            .modbusSlaveId(DEFAULT_MODBUS_SLAVE_ID)
            .modelo(DEFAULT_MODELO)
            .firmwareVersion(DEFAULT_FIRMWARE_VERSION)
            .estado(DEFAULT_ESTADO)
            .ultimoHeartbeat(DEFAULT_ULTIMO_HEARTBEAT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Equipo createUpdatedEntity() {
        return new Equipo()
            .nombre(UPDATED_NOMBRE)
            .direccionIp(UPDATED_DIRECCION_IP)
            .modbusSlaveId(UPDATED_MODBUS_SLAVE_ID)
            .modelo(UPDATED_MODELO)
            .firmwareVersion(UPDATED_FIRMWARE_VERSION)
            .estado(UPDATED_ESTADO)
            .ultimoHeartbeat(UPDATED_ULTIMO_HEARTBEAT);
    }

    @BeforeEach
    void initTest() {
        equipo = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedEquipo != null) {
            equipoRepository.delete(insertedEquipo);
            insertedEquipo = null;
        }
    }

    @Test
    @Transactional
    void createEquipo() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Equipo
        EquipoDTO equipoDTO = equipoMapper.toDto(equipo);
        var returnedEquipoDTO = om.readValue(
            restEquipoMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(equipoDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EquipoDTO.class
        );

        // Validate the Equipo in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedEquipo = equipoMapper.toEntity(returnedEquipoDTO);
        assertEquipoUpdatableFieldsEquals(returnedEquipo, getPersistedEquipo(returnedEquipo));

        insertedEquipo = returnedEquipo;
    }

    @Test
    @Transactional
    void createEquipoWithExistingId() throws Exception {
        // Create the Equipo with an existing ID
        equipo.setId(1L);
        EquipoDTO equipoDTO = equipoMapper.toDto(equipo);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEquipoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(equipoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Equipo in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        equipo.setNombre(null);

        // Create the Equipo, which fails.
        EquipoDTO equipoDTO = equipoMapper.toDto(equipo);

        restEquipoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(equipoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDireccionIpIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        equipo.setDireccionIp(null);

        // Create the Equipo, which fails.
        EquipoDTO equipoDTO = equipoMapper.toDto(equipo);

        restEquipoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(equipoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEstadoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        equipo.setEstado(null);

        // Create the Equipo, which fails.
        EquipoDTO equipoDTO = equipoMapper.toDto(equipo);

        restEquipoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(equipoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllEquipos() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList
        restEquipoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(equipo.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].direccionIp").value(hasItem(DEFAULT_DIRECCION_IP)))
            .andExpect(jsonPath("$.[*].modbusSlaveId").value(hasItem(DEFAULT_MODBUS_SLAVE_ID)))
            .andExpect(jsonPath("$.[*].modelo").value(hasItem(DEFAULT_MODELO)))
            .andExpect(jsonPath("$.[*].firmwareVersion").value(hasItem(DEFAULT_FIRMWARE_VERSION)))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())))
            .andExpect(jsonPath("$.[*].ultimoHeartbeat").value(hasItem(sameInstant(DEFAULT_ULTIMO_HEARTBEAT))));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEquiposWithEagerRelationshipsIsEnabled() throws Exception {
        when(equipoServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restEquipoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(equipoServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEquiposWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(equipoServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restEquipoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(equipoRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getEquipo() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get the equipo
        restEquipoMockMvc
            .perform(get(ENTITY_API_URL_ID, equipo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(equipo.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.direccionIp").value(DEFAULT_DIRECCION_IP))
            .andExpect(jsonPath("$.modbusSlaveId").value(DEFAULT_MODBUS_SLAVE_ID))
            .andExpect(jsonPath("$.modelo").value(DEFAULT_MODELO))
            .andExpect(jsonPath("$.firmwareVersion").value(DEFAULT_FIRMWARE_VERSION))
            .andExpect(jsonPath("$.estado").value(DEFAULT_ESTADO.toString()))
            .andExpect(jsonPath("$.ultimoHeartbeat").value(sameInstant(DEFAULT_ULTIMO_HEARTBEAT)));
    }

    @Test
    @Transactional
    void getEquiposByIdFiltering() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        Long id = equipo.getId();

        defaultEquipoFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultEquipoFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultEquipoFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllEquiposByNombreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where nombre equals to
        defaultEquipoFiltering("nombre.equals=" + DEFAULT_NOMBRE, "nombre.equals=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllEquiposByNombreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where nombre in
        defaultEquipoFiltering("nombre.in=" + DEFAULT_NOMBRE + "," + UPDATED_NOMBRE, "nombre.in=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllEquiposByNombreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where nombre is not null
        defaultEquipoFiltering("nombre.specified=true", "nombre.specified=false");
    }

    @Test
    @Transactional
    void getAllEquiposByNombreContainsSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where nombre contains
        defaultEquipoFiltering("nombre.contains=" + DEFAULT_NOMBRE, "nombre.contains=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllEquiposByNombreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where nombre does not contain
        defaultEquipoFiltering("nombre.doesNotContain=" + UPDATED_NOMBRE, "nombre.doesNotContain=" + DEFAULT_NOMBRE);
    }

    @Test
    @Transactional
    void getAllEquiposByDireccionIpIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where direccionIp equals to
        defaultEquipoFiltering("direccionIp.equals=" + DEFAULT_DIRECCION_IP, "direccionIp.equals=" + UPDATED_DIRECCION_IP);
    }

    @Test
    @Transactional
    void getAllEquiposByDireccionIpIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where direccionIp in
        defaultEquipoFiltering(
            "direccionIp.in=" + DEFAULT_DIRECCION_IP + "," + UPDATED_DIRECCION_IP,
            "direccionIp.in=" + UPDATED_DIRECCION_IP
        );
    }

    @Test
    @Transactional
    void getAllEquiposByDireccionIpIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where direccionIp is not null
        defaultEquipoFiltering("direccionIp.specified=true", "direccionIp.specified=false");
    }

    @Test
    @Transactional
    void getAllEquiposByDireccionIpContainsSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where direccionIp contains
        defaultEquipoFiltering("direccionIp.contains=" + DEFAULT_DIRECCION_IP, "direccionIp.contains=" + UPDATED_DIRECCION_IP);
    }

    @Test
    @Transactional
    void getAllEquiposByDireccionIpNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where direccionIp does not contain
        defaultEquipoFiltering("direccionIp.doesNotContain=" + UPDATED_DIRECCION_IP, "direccionIp.doesNotContain=" + DEFAULT_DIRECCION_IP);
    }

    @Test
    @Transactional
    void getAllEquiposByModbusSlaveIdIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where modbusSlaveId equals to
        defaultEquipoFiltering("modbusSlaveId.equals=" + DEFAULT_MODBUS_SLAVE_ID, "modbusSlaveId.equals=" + UPDATED_MODBUS_SLAVE_ID);
    }

    @Test
    @Transactional
    void getAllEquiposByModbusSlaveIdIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where modbusSlaveId in
        defaultEquipoFiltering(
            "modbusSlaveId.in=" + DEFAULT_MODBUS_SLAVE_ID + "," + UPDATED_MODBUS_SLAVE_ID,
            "modbusSlaveId.in=" + UPDATED_MODBUS_SLAVE_ID
        );
    }

    @Test
    @Transactional
    void getAllEquiposByModbusSlaveIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where modbusSlaveId is not null
        defaultEquipoFiltering("modbusSlaveId.specified=true", "modbusSlaveId.specified=false");
    }

    @Test
    @Transactional
    void getAllEquiposByModbusSlaveIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where modbusSlaveId is greater than or equal to
        defaultEquipoFiltering(
            "modbusSlaveId.greaterThanOrEqual=" + DEFAULT_MODBUS_SLAVE_ID,
            "modbusSlaveId.greaterThanOrEqual=" + UPDATED_MODBUS_SLAVE_ID
        );
    }

    @Test
    @Transactional
    void getAllEquiposByModbusSlaveIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where modbusSlaveId is less than or equal to
        defaultEquipoFiltering(
            "modbusSlaveId.lessThanOrEqual=" + DEFAULT_MODBUS_SLAVE_ID,
            "modbusSlaveId.lessThanOrEqual=" + SMALLER_MODBUS_SLAVE_ID
        );
    }

    @Test
    @Transactional
    void getAllEquiposByModbusSlaveIdIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where modbusSlaveId is less than
        defaultEquipoFiltering("modbusSlaveId.lessThan=" + UPDATED_MODBUS_SLAVE_ID, "modbusSlaveId.lessThan=" + DEFAULT_MODBUS_SLAVE_ID);
    }

    @Test
    @Transactional
    void getAllEquiposByModbusSlaveIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where modbusSlaveId is greater than
        defaultEquipoFiltering(
            "modbusSlaveId.greaterThan=" + SMALLER_MODBUS_SLAVE_ID,
            "modbusSlaveId.greaterThan=" + DEFAULT_MODBUS_SLAVE_ID
        );
    }

    @Test
    @Transactional
    void getAllEquiposByModeloIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where modelo equals to
        defaultEquipoFiltering("modelo.equals=" + DEFAULT_MODELO, "modelo.equals=" + UPDATED_MODELO);
    }

    @Test
    @Transactional
    void getAllEquiposByModeloIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where modelo in
        defaultEquipoFiltering("modelo.in=" + DEFAULT_MODELO + "," + UPDATED_MODELO, "modelo.in=" + UPDATED_MODELO);
    }

    @Test
    @Transactional
    void getAllEquiposByModeloIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where modelo is not null
        defaultEquipoFiltering("modelo.specified=true", "modelo.specified=false");
    }

    @Test
    @Transactional
    void getAllEquiposByModeloContainsSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where modelo contains
        defaultEquipoFiltering("modelo.contains=" + DEFAULT_MODELO, "modelo.contains=" + UPDATED_MODELO);
    }

    @Test
    @Transactional
    void getAllEquiposByModeloNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where modelo does not contain
        defaultEquipoFiltering("modelo.doesNotContain=" + UPDATED_MODELO, "modelo.doesNotContain=" + DEFAULT_MODELO);
    }

    @Test
    @Transactional
    void getAllEquiposByFirmwareVersionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where firmwareVersion equals to
        defaultEquipoFiltering("firmwareVersion.equals=" + DEFAULT_FIRMWARE_VERSION, "firmwareVersion.equals=" + UPDATED_FIRMWARE_VERSION);
    }

    @Test
    @Transactional
    void getAllEquiposByFirmwareVersionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where firmwareVersion in
        defaultEquipoFiltering(
            "firmwareVersion.in=" + DEFAULT_FIRMWARE_VERSION + "," + UPDATED_FIRMWARE_VERSION,
            "firmwareVersion.in=" + UPDATED_FIRMWARE_VERSION
        );
    }

    @Test
    @Transactional
    void getAllEquiposByFirmwareVersionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where firmwareVersion is not null
        defaultEquipoFiltering("firmwareVersion.specified=true", "firmwareVersion.specified=false");
    }

    @Test
    @Transactional
    void getAllEquiposByFirmwareVersionContainsSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where firmwareVersion contains
        defaultEquipoFiltering(
            "firmwareVersion.contains=" + DEFAULT_FIRMWARE_VERSION,
            "firmwareVersion.contains=" + UPDATED_FIRMWARE_VERSION
        );
    }

    @Test
    @Transactional
    void getAllEquiposByFirmwareVersionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where firmwareVersion does not contain
        defaultEquipoFiltering(
            "firmwareVersion.doesNotContain=" + UPDATED_FIRMWARE_VERSION,
            "firmwareVersion.doesNotContain=" + DEFAULT_FIRMWARE_VERSION
        );
    }

    @Test
    @Transactional
    void getAllEquiposByEstadoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where estado equals to
        defaultEquipoFiltering("estado.equals=" + DEFAULT_ESTADO, "estado.equals=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllEquiposByEstadoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where estado in
        defaultEquipoFiltering("estado.in=" + DEFAULT_ESTADO + "," + UPDATED_ESTADO, "estado.in=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllEquiposByEstadoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where estado is not null
        defaultEquipoFiltering("estado.specified=true", "estado.specified=false");
    }

    @Test
    @Transactional
    void getAllEquiposByUltimoHeartbeatIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where ultimoHeartbeat equals to
        defaultEquipoFiltering("ultimoHeartbeat.equals=" + DEFAULT_ULTIMO_HEARTBEAT, "ultimoHeartbeat.equals=" + UPDATED_ULTIMO_HEARTBEAT);
    }

    @Test
    @Transactional
    void getAllEquiposByUltimoHeartbeatIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where ultimoHeartbeat in
        defaultEquipoFiltering(
            "ultimoHeartbeat.in=" + DEFAULT_ULTIMO_HEARTBEAT + "," + UPDATED_ULTIMO_HEARTBEAT,
            "ultimoHeartbeat.in=" + UPDATED_ULTIMO_HEARTBEAT
        );
    }

    @Test
    @Transactional
    void getAllEquiposByUltimoHeartbeatIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where ultimoHeartbeat is not null
        defaultEquipoFiltering("ultimoHeartbeat.specified=true", "ultimoHeartbeat.specified=false");
    }

    @Test
    @Transactional
    void getAllEquiposByUltimoHeartbeatIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where ultimoHeartbeat is greater than or equal to
        defaultEquipoFiltering(
            "ultimoHeartbeat.greaterThanOrEqual=" + DEFAULT_ULTIMO_HEARTBEAT,
            "ultimoHeartbeat.greaterThanOrEqual=" + UPDATED_ULTIMO_HEARTBEAT
        );
    }

    @Test
    @Transactional
    void getAllEquiposByUltimoHeartbeatIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where ultimoHeartbeat is less than or equal to
        defaultEquipoFiltering(
            "ultimoHeartbeat.lessThanOrEqual=" + DEFAULT_ULTIMO_HEARTBEAT,
            "ultimoHeartbeat.lessThanOrEqual=" + SMALLER_ULTIMO_HEARTBEAT
        );
    }

    @Test
    @Transactional
    void getAllEquiposByUltimoHeartbeatIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where ultimoHeartbeat is less than
        defaultEquipoFiltering(
            "ultimoHeartbeat.lessThan=" + UPDATED_ULTIMO_HEARTBEAT,
            "ultimoHeartbeat.lessThan=" + DEFAULT_ULTIMO_HEARTBEAT
        );
    }

    @Test
    @Transactional
    void getAllEquiposByUltimoHeartbeatIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        // Get all the equipoList where ultimoHeartbeat is greater than
        defaultEquipoFiltering(
            "ultimoHeartbeat.greaterThan=" + SMALLER_ULTIMO_HEARTBEAT,
            "ultimoHeartbeat.greaterThan=" + DEFAULT_ULTIMO_HEARTBEAT
        );
    }

    @Test
    @Transactional
    void getAllEquiposBySitioIsEqualToSomething() throws Exception {
        Sitio sitio;
        if (TestUtil.findAll(em, Sitio.class).isEmpty()) {
            equipoRepository.saveAndFlush(equipo);
            sitio = SitioResourceIT.createEntity();
        } else {
            sitio = TestUtil.findAll(em, Sitio.class).get(0);
        }
        em.persist(sitio);
        em.flush();
        equipo.setSitio(sitio);
        equipoRepository.saveAndFlush(equipo);
        Long sitioId = sitio.getId();
        // Get all the equipoList where sitio equals to sitioId
        defaultEquipoShouldBeFound("sitioId.equals=" + sitioId);

        // Get all the equipoList where sitio equals to (sitioId + 1)
        defaultEquipoShouldNotBeFound("sitioId.equals=" + (sitioId + 1));
    }

    @Test
    @Transactional
    void getAllEquiposByEspecialidadesIsEqualToSomething() throws Exception {
        Especialidad especialidades;
        if (TestUtil.findAll(em, Especialidad.class).isEmpty()) {
            equipoRepository.saveAndFlush(equipo);
            especialidades = EspecialidadResourceIT.createEntity();
        } else {
            especialidades = TestUtil.findAll(em, Especialidad.class).get(0);
        }
        em.persist(especialidades);
        em.flush();
        equipo.addEspecialidades(especialidades);
        equipoRepository.saveAndFlush(equipo);
        Long especialidadesId = especialidades.getId();
        // Get all the equipoList where especialidades equals to especialidadesId
        defaultEquipoShouldBeFound("especialidadesId.equals=" + especialidadesId);

        // Get all the equipoList where especialidades equals to (especialidadesId + 1)
        defaultEquipoShouldNotBeFound("especialidadesId.equals=" + (especialidadesId + 1));
    }

    private void defaultEquipoFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultEquipoShouldBeFound(shouldBeFound);
        defaultEquipoShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultEquipoShouldBeFound(String filter) throws Exception {
        restEquipoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(equipo.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].direccionIp").value(hasItem(DEFAULT_DIRECCION_IP)))
            .andExpect(jsonPath("$.[*].modbusSlaveId").value(hasItem(DEFAULT_MODBUS_SLAVE_ID)))
            .andExpect(jsonPath("$.[*].modelo").value(hasItem(DEFAULT_MODELO)))
            .andExpect(jsonPath("$.[*].firmwareVersion").value(hasItem(DEFAULT_FIRMWARE_VERSION)))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())))
            .andExpect(jsonPath("$.[*].ultimoHeartbeat").value(hasItem(sameInstant(DEFAULT_ULTIMO_HEARTBEAT))));

        // Check, that the count call also returns 1
        restEquipoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultEquipoShouldNotBeFound(String filter) throws Exception {
        restEquipoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restEquipoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingEquipo() throws Exception {
        // Get the equipo
        restEquipoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEquipo() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the equipo
        Equipo updatedEquipo = equipoRepository.findById(equipo.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEquipo are not directly saved in db
        em.detach(updatedEquipo);
        updatedEquipo
            .nombre(UPDATED_NOMBRE)
            .direccionIp(UPDATED_DIRECCION_IP)
            .modbusSlaveId(UPDATED_MODBUS_SLAVE_ID)
            .modelo(UPDATED_MODELO)
            .firmwareVersion(UPDATED_FIRMWARE_VERSION)
            .estado(UPDATED_ESTADO)
            .ultimoHeartbeat(UPDATED_ULTIMO_HEARTBEAT);
        EquipoDTO equipoDTO = equipoMapper.toDto(updatedEquipo);

        restEquipoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, equipoDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(equipoDTO))
            )
            .andExpect(status().isOk());

        // Validate the Equipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEquipoToMatchAllProperties(updatedEquipo);
    }

    @Test
    @Transactional
    void putNonExistingEquipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        equipo.setId(longCount.incrementAndGet());

        // Create the Equipo
        EquipoDTO equipoDTO = equipoMapper.toDto(equipo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEquipoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, equipoDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(equipoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Equipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEquipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        equipo.setId(longCount.incrementAndGet());

        // Create the Equipo
        EquipoDTO equipoDTO = equipoMapper.toDto(equipo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEquipoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(equipoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Equipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEquipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        equipo.setId(longCount.incrementAndGet());

        // Create the Equipo
        EquipoDTO equipoDTO = equipoMapper.toDto(equipo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEquipoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(equipoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Equipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEquipoWithPatch() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the equipo using partial update
        Equipo partialUpdatedEquipo = new Equipo();
        partialUpdatedEquipo.setId(equipo.getId());

        partialUpdatedEquipo.direccionIp(UPDATED_DIRECCION_IP).modelo(UPDATED_MODELO).ultimoHeartbeat(UPDATED_ULTIMO_HEARTBEAT);

        restEquipoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEquipo.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEquipo))
            )
            .andExpect(status().isOk());

        // Validate the Equipo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEquipoUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedEquipo, equipo), getPersistedEquipo(equipo));
    }

    @Test
    @Transactional
    void fullUpdateEquipoWithPatch() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the equipo using partial update
        Equipo partialUpdatedEquipo = new Equipo();
        partialUpdatedEquipo.setId(equipo.getId());

        partialUpdatedEquipo
            .nombre(UPDATED_NOMBRE)
            .direccionIp(UPDATED_DIRECCION_IP)
            .modbusSlaveId(UPDATED_MODBUS_SLAVE_ID)
            .modelo(UPDATED_MODELO)
            .firmwareVersion(UPDATED_FIRMWARE_VERSION)
            .estado(UPDATED_ESTADO)
            .ultimoHeartbeat(UPDATED_ULTIMO_HEARTBEAT);

        restEquipoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEquipo.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEquipo))
            )
            .andExpect(status().isOk());

        // Validate the Equipo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEquipoUpdatableFieldsEquals(partialUpdatedEquipo, getPersistedEquipo(partialUpdatedEquipo));
    }

    @Test
    @Transactional
    void patchNonExistingEquipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        equipo.setId(longCount.incrementAndGet());

        // Create the Equipo
        EquipoDTO equipoDTO = equipoMapper.toDto(equipo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEquipoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, equipoDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(equipoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Equipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEquipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        equipo.setId(longCount.incrementAndGet());

        // Create the Equipo
        EquipoDTO equipoDTO = equipoMapper.toDto(equipo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEquipoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(equipoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Equipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEquipo() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        equipo.setId(longCount.incrementAndGet());

        // Create the Equipo
        EquipoDTO equipoDTO = equipoMapper.toDto(equipo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEquipoMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(equipoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Equipo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEquipo() throws Exception {
        // Initialize the database
        insertedEquipo = equipoRepository.saveAndFlush(equipo);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the equipo
        restEquipoMockMvc
            .perform(delete(ENTITY_API_URL_ID, equipo.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return equipoRepository.count();
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

    protected Equipo getPersistedEquipo(Equipo equipo) {
        return equipoRepository.findById(equipo.getId()).orElseThrow();
    }

    protected void assertPersistedEquipoToMatchAllProperties(Equipo expectedEquipo) {
        assertEquipoAllPropertiesEquals(expectedEquipo, getPersistedEquipo(expectedEquipo));
    }

    protected void assertPersistedEquipoToMatchUpdatableProperties(Equipo expectedEquipo) {
        assertEquipoAllUpdatablePropertiesEquals(expectedEquipo, getPersistedEquipo(expectedEquipo));
    }
}
