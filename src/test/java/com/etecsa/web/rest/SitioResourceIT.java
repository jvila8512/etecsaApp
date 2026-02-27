package com.etecsa.web.rest;

import static com.etecsa.domain.SitioAsserts.*;
import static com.etecsa.web.rest.TestUtil.createUpdateProxyForBean;
import static com.etecsa.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.etecsa.IntegrationTest;
import com.etecsa.domain.Sitio;
import com.etecsa.repository.SitioRepository;
import com.etecsa.service.dto.SitioDTO;
import com.etecsa.service.mapper.SitioMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link SitioResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SitioResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_CODIGO = "AAAAAAAAAA";
    private static final String UPDATED_CODIGO = "BBBBBBBBBB";

    private static final String DEFAULT_UBICACION = "AAAAAAAAAA";
    private static final String UPDATED_UBICACION = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_FECHA_REGISTRO = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_FECHA_REGISTRO = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_FECHA_REGISTRO = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final String ENTITY_API_URL = "/api/sitios";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SitioRepository sitioRepository;

    @Autowired
    private SitioMapper sitioMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSitioMockMvc;

    private Sitio sitio;

    private Sitio insertedSitio;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Sitio createEntity() {
        return new Sitio().nombre(DEFAULT_NOMBRE).codigo(DEFAULT_CODIGO).ubicacion(DEFAULT_UBICACION).fechaRegistro(DEFAULT_FECHA_REGISTRO);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Sitio createUpdatedEntity() {
        return new Sitio().nombre(UPDATED_NOMBRE).codigo(UPDATED_CODIGO).ubicacion(UPDATED_UBICACION).fechaRegistro(UPDATED_FECHA_REGISTRO);
    }

    @BeforeEach
    void initTest() {
        sitio = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedSitio != null) {
            sitioRepository.delete(insertedSitio);
            insertedSitio = null;
        }
    }

    @Test
    @Transactional
    void createSitio() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Sitio
        SitioDTO sitioDTO = sitioMapper.toDto(sitio);
        var returnedSitioDTO = om.readValue(
            restSitioMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sitioDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SitioDTO.class
        );

        // Validate the Sitio in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedSitio = sitioMapper.toEntity(returnedSitioDTO);
        assertSitioUpdatableFieldsEquals(returnedSitio, getPersistedSitio(returnedSitio));

        insertedSitio = returnedSitio;
    }

    @Test
    @Transactional
    void createSitioWithExistingId() throws Exception {
        // Create the Sitio with an existing ID
        sitio.setId(1L);
        SitioDTO sitioDTO = sitioMapper.toDto(sitio);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSitioMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sitioDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Sitio in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        sitio.setNombre(null);

        // Create the Sitio, which fails.
        SitioDTO sitioDTO = sitioMapper.toDto(sitio);

        restSitioMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sitioDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCodigoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        sitio.setCodigo(null);

        // Create the Sitio, which fails.
        SitioDTO sitioDTO = sitioMapper.toDto(sitio);

        restSitioMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sitioDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkUbicacionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        sitio.setUbicacion(null);

        // Create the Sitio, which fails.
        SitioDTO sitioDTO = sitioMapper.toDto(sitio);

        restSitioMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sitioDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSitios() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList
        restSitioMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sitio.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].codigo").value(hasItem(DEFAULT_CODIGO)))
            .andExpect(jsonPath("$.[*].ubicacion").value(hasItem(DEFAULT_UBICACION)))
            .andExpect(jsonPath("$.[*].fechaRegistro").value(hasItem(sameInstant(DEFAULT_FECHA_REGISTRO))));
    }

    @Test
    @Transactional
    void getSitio() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get the sitio
        restSitioMockMvc
            .perform(get(ENTITY_API_URL_ID, sitio.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(sitio.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.codigo").value(DEFAULT_CODIGO))
            .andExpect(jsonPath("$.ubicacion").value(DEFAULT_UBICACION))
            .andExpect(jsonPath("$.fechaRegistro").value(sameInstant(DEFAULT_FECHA_REGISTRO)));
    }

    @Test
    @Transactional
    void getSitiosByIdFiltering() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        Long id = sitio.getId();

        defaultSitioFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultSitioFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultSitioFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllSitiosByNombreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where nombre equals to
        defaultSitioFiltering("nombre.equals=" + DEFAULT_NOMBRE, "nombre.equals=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllSitiosByNombreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where nombre in
        defaultSitioFiltering("nombre.in=" + DEFAULT_NOMBRE + "," + UPDATED_NOMBRE, "nombre.in=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllSitiosByNombreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where nombre is not null
        defaultSitioFiltering("nombre.specified=true", "nombre.specified=false");
    }

    @Test
    @Transactional
    void getAllSitiosByNombreContainsSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where nombre contains
        defaultSitioFiltering("nombre.contains=" + DEFAULT_NOMBRE, "nombre.contains=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllSitiosByNombreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where nombre does not contain
        defaultSitioFiltering("nombre.doesNotContain=" + UPDATED_NOMBRE, "nombre.doesNotContain=" + DEFAULT_NOMBRE);
    }

    @Test
    @Transactional
    void getAllSitiosByCodigoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where codigo equals to
        defaultSitioFiltering("codigo.equals=" + DEFAULT_CODIGO, "codigo.equals=" + UPDATED_CODIGO);
    }

    @Test
    @Transactional
    void getAllSitiosByCodigoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where codigo in
        defaultSitioFiltering("codigo.in=" + DEFAULT_CODIGO + "," + UPDATED_CODIGO, "codigo.in=" + UPDATED_CODIGO);
    }

    @Test
    @Transactional
    void getAllSitiosByCodigoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where codigo is not null
        defaultSitioFiltering("codigo.specified=true", "codigo.specified=false");
    }

    @Test
    @Transactional
    void getAllSitiosByCodigoContainsSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where codigo contains
        defaultSitioFiltering("codigo.contains=" + DEFAULT_CODIGO, "codigo.contains=" + UPDATED_CODIGO);
    }

    @Test
    @Transactional
    void getAllSitiosByCodigoNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where codigo does not contain
        defaultSitioFiltering("codigo.doesNotContain=" + UPDATED_CODIGO, "codigo.doesNotContain=" + DEFAULT_CODIGO);
    }

    @Test
    @Transactional
    void getAllSitiosByUbicacionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where ubicacion equals to
        defaultSitioFiltering("ubicacion.equals=" + DEFAULT_UBICACION, "ubicacion.equals=" + UPDATED_UBICACION);
    }

    @Test
    @Transactional
    void getAllSitiosByUbicacionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where ubicacion in
        defaultSitioFiltering("ubicacion.in=" + DEFAULT_UBICACION + "," + UPDATED_UBICACION, "ubicacion.in=" + UPDATED_UBICACION);
    }

    @Test
    @Transactional
    void getAllSitiosByUbicacionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where ubicacion is not null
        defaultSitioFiltering("ubicacion.specified=true", "ubicacion.specified=false");
    }

    @Test
    @Transactional
    void getAllSitiosByUbicacionContainsSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where ubicacion contains
        defaultSitioFiltering("ubicacion.contains=" + DEFAULT_UBICACION, "ubicacion.contains=" + UPDATED_UBICACION);
    }

    @Test
    @Transactional
    void getAllSitiosByUbicacionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where ubicacion does not contain
        defaultSitioFiltering("ubicacion.doesNotContain=" + UPDATED_UBICACION, "ubicacion.doesNotContain=" + DEFAULT_UBICACION);
    }

    @Test
    @Transactional
    void getAllSitiosByFechaRegistroIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where fechaRegistro equals to
        defaultSitioFiltering("fechaRegistro.equals=" + DEFAULT_FECHA_REGISTRO, "fechaRegistro.equals=" + UPDATED_FECHA_REGISTRO);
    }

    @Test
    @Transactional
    void getAllSitiosByFechaRegistroIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where fechaRegistro in
        defaultSitioFiltering(
            "fechaRegistro.in=" + DEFAULT_FECHA_REGISTRO + "," + UPDATED_FECHA_REGISTRO,
            "fechaRegistro.in=" + UPDATED_FECHA_REGISTRO
        );
    }

    @Test
    @Transactional
    void getAllSitiosByFechaRegistroIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where fechaRegistro is not null
        defaultSitioFiltering("fechaRegistro.specified=true", "fechaRegistro.specified=false");
    }

    @Test
    @Transactional
    void getAllSitiosByFechaRegistroIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where fechaRegistro is greater than or equal to
        defaultSitioFiltering(
            "fechaRegistro.greaterThanOrEqual=" + DEFAULT_FECHA_REGISTRO,
            "fechaRegistro.greaterThanOrEqual=" + UPDATED_FECHA_REGISTRO
        );
    }

    @Test
    @Transactional
    void getAllSitiosByFechaRegistroIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where fechaRegistro is less than or equal to
        defaultSitioFiltering(
            "fechaRegistro.lessThanOrEqual=" + DEFAULT_FECHA_REGISTRO,
            "fechaRegistro.lessThanOrEqual=" + SMALLER_FECHA_REGISTRO
        );
    }

    @Test
    @Transactional
    void getAllSitiosByFechaRegistroIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where fechaRegistro is less than
        defaultSitioFiltering("fechaRegistro.lessThan=" + UPDATED_FECHA_REGISTRO, "fechaRegistro.lessThan=" + DEFAULT_FECHA_REGISTRO);
    }

    @Test
    @Transactional
    void getAllSitiosByFechaRegistroIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        // Get all the sitioList where fechaRegistro is greater than
        defaultSitioFiltering("fechaRegistro.greaterThan=" + SMALLER_FECHA_REGISTRO, "fechaRegistro.greaterThan=" + DEFAULT_FECHA_REGISTRO);
    }

    private void defaultSitioFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultSitioShouldBeFound(shouldBeFound);
        defaultSitioShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSitioShouldBeFound(String filter) throws Exception {
        restSitioMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sitio.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].codigo").value(hasItem(DEFAULT_CODIGO)))
            .andExpect(jsonPath("$.[*].ubicacion").value(hasItem(DEFAULT_UBICACION)))
            .andExpect(jsonPath("$.[*].fechaRegistro").value(hasItem(sameInstant(DEFAULT_FECHA_REGISTRO))));

        // Check, that the count call also returns 1
        restSitioMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSitioShouldNotBeFound(String filter) throws Exception {
        restSitioMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSitioMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSitio() throws Exception {
        // Get the sitio
        restSitioMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSitio() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the sitio
        Sitio updatedSitio = sitioRepository.findById(sitio.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSitio are not directly saved in db
        em.detach(updatedSitio);
        updatedSitio.nombre(UPDATED_NOMBRE).codigo(UPDATED_CODIGO).ubicacion(UPDATED_UBICACION).fechaRegistro(UPDATED_FECHA_REGISTRO);
        SitioDTO sitioDTO = sitioMapper.toDto(updatedSitio);

        restSitioMockMvc
            .perform(
                put(ENTITY_API_URL_ID, sitioDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sitioDTO))
            )
            .andExpect(status().isOk());

        // Validate the Sitio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSitioToMatchAllProperties(updatedSitio);
    }

    @Test
    @Transactional
    void putNonExistingSitio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        sitio.setId(longCount.incrementAndGet());

        // Create the Sitio
        SitioDTO sitioDTO = sitioMapper.toDto(sitio);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSitioMockMvc
            .perform(
                put(ENTITY_API_URL_ID, sitioDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sitioDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sitio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSitio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        sitio.setId(longCount.incrementAndGet());

        // Create the Sitio
        SitioDTO sitioDTO = sitioMapper.toDto(sitio);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSitioMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(sitioDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sitio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSitio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        sitio.setId(longCount.incrementAndGet());

        // Create the Sitio
        SitioDTO sitioDTO = sitioMapper.toDto(sitio);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSitioMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sitioDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Sitio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSitioWithPatch() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the sitio using partial update
        Sitio partialUpdatedSitio = new Sitio();
        partialUpdatedSitio.setId(sitio.getId());

        partialUpdatedSitio.nombre(UPDATED_NOMBRE).codigo(UPDATED_CODIGO).fechaRegistro(UPDATED_FECHA_REGISTRO);

        restSitioMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSitio.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSitio))
            )
            .andExpect(status().isOk());

        // Validate the Sitio in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSitioUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedSitio, sitio), getPersistedSitio(sitio));
    }

    @Test
    @Transactional
    void fullUpdateSitioWithPatch() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the sitio using partial update
        Sitio partialUpdatedSitio = new Sitio();
        partialUpdatedSitio.setId(sitio.getId());

        partialUpdatedSitio
            .nombre(UPDATED_NOMBRE)
            .codigo(UPDATED_CODIGO)
            .ubicacion(UPDATED_UBICACION)
            .fechaRegistro(UPDATED_FECHA_REGISTRO);

        restSitioMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSitio.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSitio))
            )
            .andExpect(status().isOk());

        // Validate the Sitio in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSitioUpdatableFieldsEquals(partialUpdatedSitio, getPersistedSitio(partialUpdatedSitio));
    }

    @Test
    @Transactional
    void patchNonExistingSitio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        sitio.setId(longCount.incrementAndGet());

        // Create the Sitio
        SitioDTO sitioDTO = sitioMapper.toDto(sitio);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSitioMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, sitioDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(sitioDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sitio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSitio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        sitio.setId(longCount.incrementAndGet());

        // Create the Sitio
        SitioDTO sitioDTO = sitioMapper.toDto(sitio);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSitioMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(sitioDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sitio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSitio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        sitio.setId(longCount.incrementAndGet());

        // Create the Sitio
        SitioDTO sitioDTO = sitioMapper.toDto(sitio);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSitioMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(sitioDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Sitio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSitio() throws Exception {
        // Initialize the database
        insertedSitio = sitioRepository.saveAndFlush(sitio);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the sitio
        restSitioMockMvc
            .perform(delete(ENTITY_API_URL_ID, sitio.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return sitioRepository.count();
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

    protected Sitio getPersistedSitio(Sitio sitio) {
        return sitioRepository.findById(sitio.getId()).orElseThrow();
    }

    protected void assertPersistedSitioToMatchAllProperties(Sitio expectedSitio) {
        assertSitioAllPropertiesEquals(expectedSitio, getPersistedSitio(expectedSitio));
    }

    protected void assertPersistedSitioToMatchUpdatableProperties(Sitio expectedSitio) {
        assertSitioAllUpdatablePropertiesEquals(expectedSitio, getPersistedSitio(expectedSitio));
    }
}
