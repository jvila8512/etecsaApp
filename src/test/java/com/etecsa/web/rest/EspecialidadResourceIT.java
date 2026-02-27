package com.etecsa.web.rest;

import static com.etecsa.domain.EspecialidadAsserts.*;
import static com.etecsa.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.etecsa.IntegrationTest;
import com.etecsa.domain.Equipo;
import com.etecsa.domain.Especialidad;
import com.etecsa.repository.EspecialidadRepository;
import com.etecsa.service.dto.EspecialidadDTO;
import com.etecsa.service.mapper.EspecialidadMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link EspecialidadResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class EspecialidadResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_CODIGO = "AAAAAAAAAA";
    private static final String UPDATED_CODIGO = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION_TECNICA = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION_TECNICA = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/especialidads";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private EspecialidadMapper especialidadMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEspecialidadMockMvc;

    private Especialidad especialidad;

    private Especialidad insertedEspecialidad;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Especialidad createEntity() {
        return new Especialidad().nombre(DEFAULT_NOMBRE).codigo(DEFAULT_CODIGO).descripcionTecnica(DEFAULT_DESCRIPCION_TECNICA);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Especialidad createUpdatedEntity() {
        return new Especialidad().nombre(UPDATED_NOMBRE).codigo(UPDATED_CODIGO).descripcionTecnica(UPDATED_DESCRIPCION_TECNICA);
    }

    @BeforeEach
    void initTest() {
        especialidad = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedEspecialidad != null) {
            especialidadRepository.delete(insertedEspecialidad);
            insertedEspecialidad = null;
        }
    }

    @Test
    @Transactional
    void createEspecialidad() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Especialidad
        EspecialidadDTO especialidadDTO = especialidadMapper.toDto(especialidad);
        var returnedEspecialidadDTO = om.readValue(
            restEspecialidadMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(especialidadDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EspecialidadDTO.class
        );

        // Validate the Especialidad in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedEspecialidad = especialidadMapper.toEntity(returnedEspecialidadDTO);
        assertEspecialidadUpdatableFieldsEquals(returnedEspecialidad, getPersistedEspecialidad(returnedEspecialidad));

        insertedEspecialidad = returnedEspecialidad;
    }

    @Test
    @Transactional
    void createEspecialidadWithExistingId() throws Exception {
        // Create the Especialidad with an existing ID
        especialidad.setId(1L);
        EspecialidadDTO especialidadDTO = especialidadMapper.toDto(especialidad);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEspecialidadMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(especialidadDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Especialidad in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        especialidad.setNombre(null);

        // Create the Especialidad, which fails.
        EspecialidadDTO especialidadDTO = especialidadMapper.toDto(especialidad);

        restEspecialidadMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(especialidadDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCodigoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        especialidad.setCodigo(null);

        // Create the Especialidad, which fails.
        EspecialidadDTO especialidadDTO = especialidadMapper.toDto(especialidad);

        restEspecialidadMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(especialidadDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllEspecialidads() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList
        restEspecialidadMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(especialidad.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].codigo").value(hasItem(DEFAULT_CODIGO)))
            .andExpect(jsonPath("$.[*].descripcionTecnica").value(hasItem(DEFAULT_DESCRIPCION_TECNICA)));
    }

    @Test
    @Transactional
    void getEspecialidad() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get the especialidad
        restEspecialidadMockMvc
            .perform(get(ENTITY_API_URL_ID, especialidad.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(especialidad.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.codigo").value(DEFAULT_CODIGO))
            .andExpect(jsonPath("$.descripcionTecnica").value(DEFAULT_DESCRIPCION_TECNICA));
    }

    @Test
    @Transactional
    void getEspecialidadsByIdFiltering() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        Long id = especialidad.getId();

        defaultEspecialidadFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultEspecialidadFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultEspecialidadFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllEspecialidadsByNombreIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where nombre equals to
        defaultEspecialidadFiltering("nombre.equals=" + DEFAULT_NOMBRE, "nombre.equals=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllEspecialidadsByNombreIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where nombre in
        defaultEspecialidadFiltering("nombre.in=" + DEFAULT_NOMBRE + "," + UPDATED_NOMBRE, "nombre.in=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllEspecialidadsByNombreIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where nombre is not null
        defaultEspecialidadFiltering("nombre.specified=true", "nombre.specified=false");
    }

    @Test
    @Transactional
    void getAllEspecialidadsByNombreContainsSomething() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where nombre contains
        defaultEspecialidadFiltering("nombre.contains=" + DEFAULT_NOMBRE, "nombre.contains=" + UPDATED_NOMBRE);
    }

    @Test
    @Transactional
    void getAllEspecialidadsByNombreNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where nombre does not contain
        defaultEspecialidadFiltering("nombre.doesNotContain=" + UPDATED_NOMBRE, "nombre.doesNotContain=" + DEFAULT_NOMBRE);
    }

    @Test
    @Transactional
    void getAllEspecialidadsByCodigoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where codigo equals to
        defaultEspecialidadFiltering("codigo.equals=" + DEFAULT_CODIGO, "codigo.equals=" + UPDATED_CODIGO);
    }

    @Test
    @Transactional
    void getAllEspecialidadsByCodigoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where codigo in
        defaultEspecialidadFiltering("codigo.in=" + DEFAULT_CODIGO + "," + UPDATED_CODIGO, "codigo.in=" + UPDATED_CODIGO);
    }

    @Test
    @Transactional
    void getAllEspecialidadsByCodigoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where codigo is not null
        defaultEspecialidadFiltering("codigo.specified=true", "codigo.specified=false");
    }

    @Test
    @Transactional
    void getAllEspecialidadsByCodigoContainsSomething() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where codigo contains
        defaultEspecialidadFiltering("codigo.contains=" + DEFAULT_CODIGO, "codigo.contains=" + UPDATED_CODIGO);
    }

    @Test
    @Transactional
    void getAllEspecialidadsByCodigoNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where codigo does not contain
        defaultEspecialidadFiltering("codigo.doesNotContain=" + UPDATED_CODIGO, "codigo.doesNotContain=" + DEFAULT_CODIGO);
    }

    @Test
    @Transactional
    void getAllEspecialidadsByDescripcionTecnicaIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where descripcionTecnica equals to
        defaultEspecialidadFiltering(
            "descripcionTecnica.equals=" + DEFAULT_DESCRIPCION_TECNICA,
            "descripcionTecnica.equals=" + UPDATED_DESCRIPCION_TECNICA
        );
    }

    @Test
    @Transactional
    void getAllEspecialidadsByDescripcionTecnicaIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where descripcionTecnica in
        defaultEspecialidadFiltering(
            "descripcionTecnica.in=" + DEFAULT_DESCRIPCION_TECNICA + "," + UPDATED_DESCRIPCION_TECNICA,
            "descripcionTecnica.in=" + UPDATED_DESCRIPCION_TECNICA
        );
    }

    @Test
    @Transactional
    void getAllEspecialidadsByDescripcionTecnicaIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where descripcionTecnica is not null
        defaultEspecialidadFiltering("descripcionTecnica.specified=true", "descripcionTecnica.specified=false");
    }

    @Test
    @Transactional
    void getAllEspecialidadsByDescripcionTecnicaContainsSomething() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where descripcionTecnica contains
        defaultEspecialidadFiltering(
            "descripcionTecnica.contains=" + DEFAULT_DESCRIPCION_TECNICA,
            "descripcionTecnica.contains=" + UPDATED_DESCRIPCION_TECNICA
        );
    }

    @Test
    @Transactional
    void getAllEspecialidadsByDescripcionTecnicaNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        // Get all the especialidadList where descripcionTecnica does not contain
        defaultEspecialidadFiltering(
            "descripcionTecnica.doesNotContain=" + UPDATED_DESCRIPCION_TECNICA,
            "descripcionTecnica.doesNotContain=" + DEFAULT_DESCRIPCION_TECNICA
        );
    }

    @Test
    @Transactional
    void getAllEspecialidadsByEquiposIsEqualToSomething() throws Exception {
        Equipo equipos;
        if (TestUtil.findAll(em, Equipo.class).isEmpty()) {
            especialidadRepository.saveAndFlush(especialidad);
            equipos = EquipoResourceIT.createEntity();
        } else {
            equipos = TestUtil.findAll(em, Equipo.class).get(0);
        }
        em.persist(equipos);
        em.flush();
        especialidad.addEquipos(equipos);
        especialidadRepository.saveAndFlush(especialidad);
        Long equiposId = equipos.getId();
        // Get all the especialidadList where equipos equals to equiposId
        defaultEspecialidadShouldBeFound("equiposId.equals=" + equiposId);

        // Get all the especialidadList where equipos equals to (equiposId + 1)
        defaultEspecialidadShouldNotBeFound("equiposId.equals=" + (equiposId + 1));
    }

    private void defaultEspecialidadFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultEspecialidadShouldBeFound(shouldBeFound);
        defaultEspecialidadShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultEspecialidadShouldBeFound(String filter) throws Exception {
        restEspecialidadMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(especialidad.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].codigo").value(hasItem(DEFAULT_CODIGO)))
            .andExpect(jsonPath("$.[*].descripcionTecnica").value(hasItem(DEFAULT_DESCRIPCION_TECNICA)));

        // Check, that the count call also returns 1
        restEspecialidadMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultEspecialidadShouldNotBeFound(String filter) throws Exception {
        restEspecialidadMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restEspecialidadMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingEspecialidad() throws Exception {
        // Get the especialidad
        restEspecialidadMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEspecialidad() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the especialidad
        Especialidad updatedEspecialidad = especialidadRepository.findById(especialidad.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEspecialidad are not directly saved in db
        em.detach(updatedEspecialidad);
        updatedEspecialidad.nombre(UPDATED_NOMBRE).codigo(UPDATED_CODIGO).descripcionTecnica(UPDATED_DESCRIPCION_TECNICA);
        EspecialidadDTO especialidadDTO = especialidadMapper.toDto(updatedEspecialidad);

        restEspecialidadMockMvc
            .perform(
                put(ENTITY_API_URL_ID, especialidadDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(especialidadDTO))
            )
            .andExpect(status().isOk());

        // Validate the Especialidad in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEspecialidadToMatchAllProperties(updatedEspecialidad);
    }

    @Test
    @Transactional
    void putNonExistingEspecialidad() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        especialidad.setId(longCount.incrementAndGet());

        // Create the Especialidad
        EspecialidadDTO especialidadDTO = especialidadMapper.toDto(especialidad);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEspecialidadMockMvc
            .perform(
                put(ENTITY_API_URL_ID, especialidadDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(especialidadDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Especialidad in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEspecialidad() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        especialidad.setId(longCount.incrementAndGet());

        // Create the Especialidad
        EspecialidadDTO especialidadDTO = especialidadMapper.toDto(especialidad);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEspecialidadMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(especialidadDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Especialidad in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEspecialidad() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        especialidad.setId(longCount.incrementAndGet());

        // Create the Especialidad
        EspecialidadDTO especialidadDTO = especialidadMapper.toDto(especialidad);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEspecialidadMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(especialidadDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Especialidad in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEspecialidadWithPatch() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the especialidad using partial update
        Especialidad partialUpdatedEspecialidad = new Especialidad();
        partialUpdatedEspecialidad.setId(especialidad.getId());

        partialUpdatedEspecialidad.nombre(UPDATED_NOMBRE).codigo(UPDATED_CODIGO);

        restEspecialidadMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEspecialidad.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEspecialidad))
            )
            .andExpect(status().isOk());

        // Validate the Especialidad in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEspecialidadUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedEspecialidad, especialidad),
            getPersistedEspecialidad(especialidad)
        );
    }

    @Test
    @Transactional
    void fullUpdateEspecialidadWithPatch() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the especialidad using partial update
        Especialidad partialUpdatedEspecialidad = new Especialidad();
        partialUpdatedEspecialidad.setId(especialidad.getId());

        partialUpdatedEspecialidad.nombre(UPDATED_NOMBRE).codigo(UPDATED_CODIGO).descripcionTecnica(UPDATED_DESCRIPCION_TECNICA);

        restEspecialidadMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEspecialidad.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEspecialidad))
            )
            .andExpect(status().isOk());

        // Validate the Especialidad in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEspecialidadUpdatableFieldsEquals(partialUpdatedEspecialidad, getPersistedEspecialidad(partialUpdatedEspecialidad));
    }

    @Test
    @Transactional
    void patchNonExistingEspecialidad() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        especialidad.setId(longCount.incrementAndGet());

        // Create the Especialidad
        EspecialidadDTO especialidadDTO = especialidadMapper.toDto(especialidad);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEspecialidadMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, especialidadDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(especialidadDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Especialidad in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEspecialidad() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        especialidad.setId(longCount.incrementAndGet());

        // Create the Especialidad
        EspecialidadDTO especialidadDTO = especialidadMapper.toDto(especialidad);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEspecialidadMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(especialidadDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Especialidad in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEspecialidad() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        especialidad.setId(longCount.incrementAndGet());

        // Create the Especialidad
        EspecialidadDTO especialidadDTO = especialidadMapper.toDto(especialidad);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEspecialidadMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(especialidadDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Especialidad in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEspecialidad() throws Exception {
        // Initialize the database
        insertedEspecialidad = especialidadRepository.saveAndFlush(especialidad);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the especialidad
        restEspecialidadMockMvc
            .perform(delete(ENTITY_API_URL_ID, especialidad.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return especialidadRepository.count();
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

    protected Especialidad getPersistedEspecialidad(Especialidad especialidad) {
        return especialidadRepository.findById(especialidad.getId()).orElseThrow();
    }

    protected void assertPersistedEspecialidadToMatchAllProperties(Especialidad expectedEspecialidad) {
        assertEspecialidadAllPropertiesEquals(expectedEspecialidad, getPersistedEspecialidad(expectedEspecialidad));
    }

    protected void assertPersistedEspecialidadToMatchUpdatableProperties(Especialidad expectedEspecialidad) {
        assertEspecialidadAllUpdatablePropertiesEquals(expectedEspecialidad, getPersistedEspecialidad(expectedEspecialidad));
    }
}
