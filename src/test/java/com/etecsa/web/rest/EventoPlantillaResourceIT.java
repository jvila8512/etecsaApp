package com.etecsa.web.rest;

import static com.etecsa.domain.EventoPlantillaAsserts.*;
import static com.etecsa.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.etecsa.IntegrationTest;
import com.etecsa.domain.Especialidad;
import com.etecsa.domain.EventoPlantilla;
import com.etecsa.repository.EventoPlantillaRepository;
import com.etecsa.service.EventoPlantillaService;
import com.etecsa.service.dto.EventoPlantillaDTO;
import com.etecsa.service.mapper.EventoPlantillaMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link EventoPlantillaResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class EventoPlantillaResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final Double DEFAULT_SCALING_FACTOR = 1D;
    private static final Double UPDATED_SCALING_FACTOR = 2D;
    private static final Double SMALLER_SCALING_FACTOR = 1D - 1D;

    private static final String DEFAULT_UNIDAD_MEDIDA = "AAAAAAAAAA";
    private static final String UPDATED_UNIDAD_MEDIDA = "BBBBBBBBBB";

    private static final String DEFAULT_FUNCION_LECTURA = "AAAAAAAAAA";
    private static final String UPDATED_FUNCION_LECTURA = "BBBBBBBBBB";

    private static final String DEFAULT_FUNCION_ESCRITURA = "AAAAAAAAAA";
    private static final String UPDATED_FUNCION_ESCRITURA = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/evento-plantillas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EventoPlantillaRepository eventoPlantillaRepository;

    @Mock
    private EventoPlantillaRepository eventoPlantillaRepositoryMock;

    @Autowired
    private EventoPlantillaMapper eventoPlantillaMapper;

    @Mock
    private EventoPlantillaService eventoPlantillaServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEventoPlantillaMockMvc;

    private EventoPlantilla eventoPlantilla;

    private EventoPlantilla insertedEventoPlantilla;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventoPlantilla createEntity() {
        return new EventoPlantilla()
            .nombre(DEFAULT_NOMBRE)
            .descripcion(DEFAULT_DESCRIPCION)
            .scalingFactor(DEFAULT_SCALING_FACTOR)
            .unidadMedida(DEFAULT_UNIDAD_MEDIDA)
            .funcionLectura(DEFAULT_FUNCION_LECTURA)
            .funcionEscritura(DEFAULT_FUNCION_ESCRITURA);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static EventoPlantilla createUpdatedEntity() {
        return new EventoPlantilla()
            .nombre(UPDATED_NOMBRE)
            .descripcion(UPDATED_DESCRIPCION)
            .scalingFactor(UPDATED_SCALING_FACTOR)
            .unidadMedida(UPDATED_UNIDAD_MEDIDA)
            .funcionLectura(UPDATED_FUNCION_LECTURA)
            .funcionEscritura(UPDATED_FUNCION_ESCRITURA);
    }

    @BeforeEach
    void initTest() {
        eventoPlantilla = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedEventoPlantilla != null) {
            eventoPlantillaRepository.delete(insertedEventoPlantilla);
            insertedEventoPlantilla = null;
        }
    }

    @Test
    @Transactional
    void createEventoPlantilla() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the EventoPlantilla
        EventoPlantillaDTO eventoPlantillaDTO = eventoPlantillaMapper.toDto(eventoPlantilla);
        var returnedEventoPlantillaDTO = om.readValue(
            restEventoPlantillaMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoPlantillaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EventoPlantillaDTO.class
        );

        // Validate the EventoPlantilla in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedEventoPlantilla = eventoPlantillaMapper.toEntity(returnedEventoPlantillaDTO);
        assertEventoPlantillaUpdatableFieldsEquals(returnedEventoPlantilla, getPersistedEventoPlantilla(returnedEventoPlantilla));

        insertedEventoPlantilla = returnedEventoPlantilla;
    }

    @Test
    @Transactional
    void createEventoPlantillaWithExistingId() throws Exception {
        // Create the EventoPlantilla with an existing ID
        eventoPlantilla.setId(1L);
        EventoPlantillaDTO eventoPlantillaDTO = eventoPlantillaMapper.toDto(eventoPlantilla);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEventoPlantillaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoPlantillaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the EventoPlantilla in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        eventoPlantilla.setNombre(null);

        // Create the EventoPlantilla, which fails.
        EventoPlantillaDTO eventoPlantillaDTO = eventoPlantillaMapper.toDto(eventoPlantilla);

        restEventoPlantillaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoPlantillaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllEventoPlantillas() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList
        restEventoPlantillaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventoPlantilla.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].scalingFactor").value(hasItem(DEFAULT_SCALING_FACTOR)))
            .andExpect(jsonPath("$.[*].unidadMedida").value(hasItem(DEFAULT_UNIDAD_MEDIDA)))
            .andExpect(jsonPath("$.[*].funcionLectura").value(hasItem(DEFAULT_FUNCION_LECTURA)))
            .andExpect(jsonPath("$.[*].funcionEscritura").value(hasItem(DEFAULT_FUNCION_ESCRITURA)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEventoPlantillasWithEagerRelationshipsIsEnabled() throws Exception {
        when(eventoPlantillaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restEventoPlantillaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(eventoPlantillaServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEventoPlantillasWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(eventoPlantillaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restEventoPlantillaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(eventoPlantillaRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getEventoPlantilla() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get the eventoPlantilla
        restEventoPlantillaMockMvc
            .perform(get(ENTITY_API_URL_ID, eventoPlantilla.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(eventoPlantilla.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION))
            .andExpect(jsonPath("$.scalingFactor").value(DEFAULT_SCALING_FACTOR))
            .andExpect(jsonPath("$.unidadMedida").value(DEFAULT_UNIDAD_MEDIDA))
            .andExpect(jsonPath("$.funcionLectura").value(DEFAULT_FUNCION_LECTURA))
            .andExpect(jsonPath("$.funcionEscritura").value(DEFAULT_FUNCION_ESCRITURA));
    }

    @Test
    @Transactional
    void getEventoPlantillasByIdFiltering() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        Long id = eventoPlantilla.getId();

        defaultEventoPlantillaFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultEventoPlantillaFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultEventoPlantillaFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByNombreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where nombre equals to
        defaultEventoPlantillaFiltering("nombre.equals=" + DEFAULT_NOMBRE, "nombre.equals=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByNombreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where nombre in
        defaultEventoPlantillaFiltering("nombre.in=" + DEFAULT_NOMBRE + "," + UPDATED_NOMBRE, "nombre.in=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByNombreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where nombre is not null
        defaultEventoPlantillaFiltering("nombre.specified=true", "nombre.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByNombreContainsSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where nombre contains
        defaultEventoPlantillaFiltering("nombre.contains=" + DEFAULT_NOMBRE, "nombre.contains=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByNombreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where nombre does not contain
        defaultEventoPlantillaFiltering("nombre.doesNotContain=" + UPDATED_NOMBRE, "nombre.doesNotContain=" + DEFAULT_NOMBRE);
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByDescripcionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where descripcion equals to
        defaultEventoPlantillaFiltering("descripcion.equals=" + DEFAULT_DESCRIPCION, "descripcion.equals=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByDescripcionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where descripcion in
        defaultEventoPlantillaFiltering(
            "descripcion.in=" + DEFAULT_DESCRIPCION + "," + UPDATED_DESCRIPCION,
            "descripcion.in=" + UPDATED_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByDescripcionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where descripcion is not null
        defaultEventoPlantillaFiltering("descripcion.specified=true", "descripcion.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByDescripcionContainsSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where descripcion contains
        defaultEventoPlantillaFiltering("descripcion.contains=" + DEFAULT_DESCRIPCION, "descripcion.contains=" + UPDATED_DESCRIPCION);
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByDescripcionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where descripcion does not contain
        defaultEventoPlantillaFiltering(
            "descripcion.doesNotContain=" + UPDATED_DESCRIPCION,
            "descripcion.doesNotContain=" + DEFAULT_DESCRIPCION
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByScalingFactorIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where scalingFactor equals to
        defaultEventoPlantillaFiltering("scalingFactor.equals=" + DEFAULT_SCALING_FACTOR, "scalingFactor.equals=" + UPDATED_SCALING_FACTOR);
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByScalingFactorIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where scalingFactor in
        defaultEventoPlantillaFiltering(
            "scalingFactor.in=" + DEFAULT_SCALING_FACTOR + "," + UPDATED_SCALING_FACTOR,
            "scalingFactor.in=" + UPDATED_SCALING_FACTOR
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByScalingFactorIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where scalingFactor is not null
        defaultEventoPlantillaFiltering("scalingFactor.specified=true", "scalingFactor.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByScalingFactorIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where scalingFactor is greater than or equal to
        defaultEventoPlantillaFiltering(
            "scalingFactor.greaterThanOrEqual=" + DEFAULT_SCALING_FACTOR,
            "scalingFactor.greaterThanOrEqual=" + UPDATED_SCALING_FACTOR
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByScalingFactorIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where scalingFactor is less than or equal to
        defaultEventoPlantillaFiltering(
            "scalingFactor.lessThanOrEqual=" + DEFAULT_SCALING_FACTOR,
            "scalingFactor.lessThanOrEqual=" + SMALLER_SCALING_FACTOR
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByScalingFactorIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where scalingFactor is less than
        defaultEventoPlantillaFiltering(
            "scalingFactor.lessThan=" + UPDATED_SCALING_FACTOR,
            "scalingFactor.lessThan=" + DEFAULT_SCALING_FACTOR
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByScalingFactorIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where scalingFactor is greater than
        defaultEventoPlantillaFiltering(
            "scalingFactor.greaterThan=" + SMALLER_SCALING_FACTOR,
            "scalingFactor.greaterThan=" + DEFAULT_SCALING_FACTOR
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByUnidadMedidaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where unidadMedida equals to
        defaultEventoPlantillaFiltering("unidadMedida.equals=" + DEFAULT_UNIDAD_MEDIDA, "unidadMedida.equals=" + UPDATED_UNIDAD_MEDIDA);
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByUnidadMedidaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where unidadMedida in
        defaultEventoPlantillaFiltering(
            "unidadMedida.in=" + DEFAULT_UNIDAD_MEDIDA + "," + UPDATED_UNIDAD_MEDIDA,
            "unidadMedida.in=" + UPDATED_UNIDAD_MEDIDA
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByUnidadMedidaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where unidadMedida is not null
        defaultEventoPlantillaFiltering("unidadMedida.specified=true", "unidadMedida.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByUnidadMedidaContainsSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where unidadMedida contains
        defaultEventoPlantillaFiltering("unidadMedida.contains=" + DEFAULT_UNIDAD_MEDIDA, "unidadMedida.contains=" + UPDATED_UNIDAD_MEDIDA);
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByUnidadMedidaNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where unidadMedida does not contain
        defaultEventoPlantillaFiltering(
            "unidadMedida.doesNotContain=" + UPDATED_UNIDAD_MEDIDA,
            "unidadMedida.doesNotContain=" + DEFAULT_UNIDAD_MEDIDA
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByFuncionLecturaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where funcionLectura equals to
        defaultEventoPlantillaFiltering(
            "funcionLectura.equals=" + DEFAULT_FUNCION_LECTURA,
            "funcionLectura.equals=" + UPDATED_FUNCION_LECTURA
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByFuncionLecturaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where funcionLectura in
        defaultEventoPlantillaFiltering(
            "funcionLectura.in=" + DEFAULT_FUNCION_LECTURA + "," + UPDATED_FUNCION_LECTURA,
            "funcionLectura.in=" + UPDATED_FUNCION_LECTURA
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByFuncionLecturaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where funcionLectura is not null
        defaultEventoPlantillaFiltering("funcionLectura.specified=true", "funcionLectura.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByFuncionLecturaContainsSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where funcionLectura contains
        defaultEventoPlantillaFiltering(
            "funcionLectura.contains=" + DEFAULT_FUNCION_LECTURA,
            "funcionLectura.contains=" + UPDATED_FUNCION_LECTURA
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByFuncionLecturaNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where funcionLectura does not contain
        defaultEventoPlantillaFiltering(
            "funcionLectura.doesNotContain=" + UPDATED_FUNCION_LECTURA,
            "funcionLectura.doesNotContain=" + DEFAULT_FUNCION_LECTURA
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByFuncionEscrituraIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where funcionEscritura equals to
        defaultEventoPlantillaFiltering(
            "funcionEscritura.equals=" + DEFAULT_FUNCION_ESCRITURA,
            "funcionEscritura.equals=" + UPDATED_FUNCION_ESCRITURA
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByFuncionEscrituraIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where funcionEscritura in
        defaultEventoPlantillaFiltering(
            "funcionEscritura.in=" + DEFAULT_FUNCION_ESCRITURA + "," + UPDATED_FUNCION_ESCRITURA,
            "funcionEscritura.in=" + UPDATED_FUNCION_ESCRITURA
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByFuncionEscrituraIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where funcionEscritura is not null
        defaultEventoPlantillaFiltering("funcionEscritura.specified=true", "funcionEscritura.specified=false");
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByFuncionEscrituraContainsSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where funcionEscritura contains
        defaultEventoPlantillaFiltering(
            "funcionEscritura.contains=" + DEFAULT_FUNCION_ESCRITURA,
            "funcionEscritura.contains=" + UPDATED_FUNCION_ESCRITURA
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByFuncionEscrituraNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        // Get all the eventoPlantillaList where funcionEscritura does not contain
        defaultEventoPlantillaFiltering(
            "funcionEscritura.doesNotContain=" + UPDATED_FUNCION_ESCRITURA,
            "funcionEscritura.doesNotContain=" + DEFAULT_FUNCION_ESCRITURA
        );
    }

    @Test
    @Transactional
    void getAllEventoPlantillasByEspecialidadIsEqualToSomething() throws Exception {
        Especialidad especialidad;
        if (TestUtil.findAll(em, Especialidad.class).isEmpty()) {
            eventoPlantillaRepository.saveAndFlush(eventoPlantilla);
            especialidad = EspecialidadResourceIT.createEntity();
        } else {
            especialidad = TestUtil.findAll(em, Especialidad.class).get(0);
        }
        em.persist(especialidad);
        em.flush();
        eventoPlantilla.setEspecialidad(especialidad);
        eventoPlantillaRepository.saveAndFlush(eventoPlantilla);
        Long especialidadId = especialidad.getId();
        // Get all the eventoPlantillaList where especialidad equals to especialidadId
        defaultEventoPlantillaShouldBeFound("especialidadId.equals=" + especialidadId);

        // Get all the eventoPlantillaList where especialidad equals to (especialidadId + 1)
        defaultEventoPlantillaShouldNotBeFound("especialidadId.equals=" + (especialidadId + 1));
    }

    private void defaultEventoPlantillaFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultEventoPlantillaShouldBeFound(shouldBeFound);
        defaultEventoPlantillaShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultEventoPlantillaShouldBeFound(String filter) throws Exception {
        restEventoPlantillaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(eventoPlantilla.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].scalingFactor").value(hasItem(DEFAULT_SCALING_FACTOR)))
            .andExpect(jsonPath("$.[*].unidadMedida").value(hasItem(DEFAULT_UNIDAD_MEDIDA)))
            .andExpect(jsonPath("$.[*].funcionLectura").value(hasItem(DEFAULT_FUNCION_LECTURA)))
            .andExpect(jsonPath("$.[*].funcionEscritura").value(hasItem(DEFAULT_FUNCION_ESCRITURA)));

        // Check, that the count call also returns 1
        restEventoPlantillaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultEventoPlantillaShouldNotBeFound(String filter) throws Exception {
        restEventoPlantillaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restEventoPlantillaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingEventoPlantilla() throws Exception {
        // Get the eventoPlantilla
        restEventoPlantillaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEventoPlantilla() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventoPlantilla
        EventoPlantilla updatedEventoPlantilla = eventoPlantillaRepository.findById(eventoPlantilla.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEventoPlantilla are not directly saved in db
        em.detach(updatedEventoPlantilla);
        updatedEventoPlantilla
            .nombre(UPDATED_NOMBRE)
            .descripcion(UPDATED_DESCRIPCION)
            .scalingFactor(UPDATED_SCALING_FACTOR)
            .unidadMedida(UPDATED_UNIDAD_MEDIDA)
            .funcionLectura(UPDATED_FUNCION_LECTURA)
            .funcionEscritura(UPDATED_FUNCION_ESCRITURA);
        EventoPlantillaDTO eventoPlantillaDTO = eventoPlantillaMapper.toDto(updatedEventoPlantilla);

        restEventoPlantillaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, eventoPlantillaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(eventoPlantillaDTO))
            )
            .andExpect(status().isOk());

        // Validate the EventoPlantilla in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEventoPlantillaToMatchAllProperties(updatedEventoPlantilla);
    }

    @Test
    @Transactional
    void putNonExistingEventoPlantilla() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoPlantilla.setId(longCount.incrementAndGet());

        // Create the EventoPlantilla
        EventoPlantillaDTO eventoPlantillaDTO = eventoPlantillaMapper.toDto(eventoPlantilla);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEventoPlantillaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, eventoPlantillaDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(eventoPlantillaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventoPlantilla in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEventoPlantilla() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoPlantilla.setId(longCount.incrementAndGet());

        // Create the EventoPlantilla
        EventoPlantillaDTO eventoPlantillaDTO = eventoPlantillaMapper.toDto(eventoPlantilla);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventoPlantillaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(eventoPlantillaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventoPlantilla in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEventoPlantilla() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoPlantilla.setId(longCount.incrementAndGet());

        // Create the EventoPlantilla
        EventoPlantillaDTO eventoPlantillaDTO = eventoPlantillaMapper.toDto(eventoPlantilla);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventoPlantillaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(eventoPlantillaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the EventoPlantilla in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEventoPlantillaWithPatch() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventoPlantilla using partial update
        EventoPlantilla partialUpdatedEventoPlantilla = new EventoPlantilla();
        partialUpdatedEventoPlantilla.setId(eventoPlantilla.getId());

        partialUpdatedEventoPlantilla.descripcion(UPDATED_DESCRIPCION).funcionLectura(UPDATED_FUNCION_LECTURA);

        restEventoPlantillaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventoPlantilla.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEventoPlantilla))
            )
            .andExpect(status().isOk());

        // Validate the EventoPlantilla in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEventoPlantillaUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedEventoPlantilla, eventoPlantilla),
            getPersistedEventoPlantilla(eventoPlantilla)
        );
    }

    @Test
    @Transactional
    void fullUpdateEventoPlantillaWithPatch() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the eventoPlantilla using partial update
        EventoPlantilla partialUpdatedEventoPlantilla = new EventoPlantilla();
        partialUpdatedEventoPlantilla.setId(eventoPlantilla.getId());

        partialUpdatedEventoPlantilla
            .nombre(UPDATED_NOMBRE)
            .descripcion(UPDATED_DESCRIPCION)
            .scalingFactor(UPDATED_SCALING_FACTOR)
            .unidadMedida(UPDATED_UNIDAD_MEDIDA)
            .funcionLectura(UPDATED_FUNCION_LECTURA)
            .funcionEscritura(UPDATED_FUNCION_ESCRITURA);

        restEventoPlantillaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEventoPlantilla.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEventoPlantilla))
            )
            .andExpect(status().isOk());

        // Validate the EventoPlantilla in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEventoPlantillaUpdatableFieldsEquals(
            partialUpdatedEventoPlantilla,
            getPersistedEventoPlantilla(partialUpdatedEventoPlantilla)
        );
    }

    @Test
    @Transactional
    void patchNonExistingEventoPlantilla() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoPlantilla.setId(longCount.incrementAndGet());

        // Create the EventoPlantilla
        EventoPlantillaDTO eventoPlantillaDTO = eventoPlantillaMapper.toDto(eventoPlantilla);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEventoPlantillaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, eventoPlantillaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(eventoPlantillaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventoPlantilla in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEventoPlantilla() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoPlantilla.setId(longCount.incrementAndGet());

        // Create the EventoPlantilla
        EventoPlantillaDTO eventoPlantillaDTO = eventoPlantillaMapper.toDto(eventoPlantilla);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventoPlantillaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(eventoPlantillaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the EventoPlantilla in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEventoPlantilla() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        eventoPlantilla.setId(longCount.incrementAndGet());

        // Create the EventoPlantilla
        EventoPlantillaDTO eventoPlantillaDTO = eventoPlantillaMapper.toDto(eventoPlantilla);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEventoPlantillaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(eventoPlantillaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the EventoPlantilla in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEventoPlantilla() throws Exception {
        // Initialize the database
        insertedEventoPlantilla = eventoPlantillaRepository.saveAndFlush(eventoPlantilla);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the eventoPlantilla
        restEventoPlantillaMockMvc
            .perform(delete(ENTITY_API_URL_ID, eventoPlantilla.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return eventoPlantillaRepository.count();
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

    protected EventoPlantilla getPersistedEventoPlantilla(EventoPlantilla eventoPlantilla) {
        return eventoPlantillaRepository.findById(eventoPlantilla.getId()).orElseThrow();
    }

    protected void assertPersistedEventoPlantillaToMatchAllProperties(EventoPlantilla expectedEventoPlantilla) {
        assertEventoPlantillaAllPropertiesEquals(expectedEventoPlantilla, getPersistedEventoPlantilla(expectedEventoPlantilla));
    }

    protected void assertPersistedEventoPlantillaToMatchUpdatableProperties(EventoPlantilla expectedEventoPlantilla) {
        assertEventoPlantillaAllUpdatablePropertiesEquals(expectedEventoPlantilla, getPersistedEventoPlantilla(expectedEventoPlantilla));
    }
}
