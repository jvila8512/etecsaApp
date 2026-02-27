package com.etecsa.web.rest;

import com.etecsa.repository.EquipoRepository;
import com.etecsa.service.EquipoQueryService;
import com.etecsa.service.EquipoService;
import com.etecsa.service.criteria.EquipoCriteria;
import com.etecsa.service.dto.EquipoDTO;
import com.etecsa.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.etecsa.domain.Equipo}.
 */
@RestController
@RequestMapping("/api/equipos")
public class EquipoResource {

    private static final Logger LOG = LoggerFactory.getLogger(EquipoResource.class);

    private static final String ENTITY_NAME = "equipo";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EquipoService equipoService;

    private final EquipoRepository equipoRepository;

    private final EquipoQueryService equipoQueryService;

    public EquipoResource(EquipoService equipoService, EquipoRepository equipoRepository, EquipoQueryService equipoQueryService) {
        this.equipoService = equipoService;
        this.equipoRepository = equipoRepository;
        this.equipoQueryService = equipoQueryService;
    }

    /**
     * {@code POST  /equipos} : Create a new equipo.
     *
     * @param equipoDTO the equipoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new equipoDTO, or with status {@code 400 (Bad Request)} if the equipo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<EquipoDTO> createEquipo(@Valid @RequestBody EquipoDTO equipoDTO) throws URISyntaxException {
        LOG.debug("REST request to save Equipo : {}", equipoDTO);
        if (equipoDTO.getId() != null) {
            throw new BadRequestAlertException("A new equipo cannot already have an ID", ENTITY_NAME, "idexists");
        }
        equipoDTO = equipoService.save(equipoDTO);
        return ResponseEntity.created(new URI("/api/equipos/" + equipoDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, equipoDTO.getId().toString()))
            .body(equipoDTO);
    }

    /**
     * {@code PUT  /equipos/:id} : Updates an existing equipo.
     *
     * @param id the id of the equipoDTO to save.
     * @param equipoDTO the equipoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated equipoDTO,
     * or with status {@code 400 (Bad Request)} if the equipoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the equipoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EquipoDTO> updateEquipo(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EquipoDTO equipoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Equipo : {}, {}", id, equipoDTO);
        if (equipoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, equipoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!equipoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        equipoDTO = equipoService.update(equipoDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, equipoDTO.getId().toString()))
            .body(equipoDTO);
    }

    /**
     * {@code PATCH  /equipos/:id} : Partial updates given fields of an existing equipo, field will ignore if it is null
     *
     * @param id the id of the equipoDTO to save.
     * @param equipoDTO the equipoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated equipoDTO,
     * or with status {@code 400 (Bad Request)} if the equipoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the equipoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the equipoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EquipoDTO> partialUpdateEquipo(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EquipoDTO equipoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Equipo partially : {}, {}", id, equipoDTO);
        if (equipoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, equipoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!equipoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EquipoDTO> result = equipoService.partialUpdate(equipoDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, equipoDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /equipos} : get all the equipos.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of equipos in body.
     */
    @GetMapping("")
    public ResponseEntity<List<EquipoDTO>> getAllEquipos(
        EquipoCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Equipos by criteria: {}", criteria);

        Page<EquipoDTO> page = equipoQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /equipos/count} : count all the equipos.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countEquipos(EquipoCriteria criteria) {
        LOG.debug("REST request to count Equipos by criteria: {}", criteria);
        return ResponseEntity.ok().body(equipoQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /equipos/:id} : get the "id" equipo.
     *
     * @param id the id of the equipoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the equipoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EquipoDTO> getEquipo(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Equipo : {}", id);
        Optional<EquipoDTO> equipoDTO = equipoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(equipoDTO);
    }

    /**
     * {@code DELETE  /equipos/:id} : delete the "id" equipo.
     *
     * @param id the id of the equipoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipo(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Equipo : {}", id);
        equipoService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
