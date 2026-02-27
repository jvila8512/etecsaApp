package com.etecsa.web.rest;

import com.etecsa.repository.EventoEquipoRepository;
import com.etecsa.service.EventoEquipoQueryService;
import com.etecsa.service.EventoEquipoService;
import com.etecsa.service.criteria.EventoEquipoCriteria;
import com.etecsa.service.dto.EventoEquipoDTO;
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
 * REST controller for managing {@link com.etecsa.domain.EventoEquipo}.
 */
@RestController
@RequestMapping("/api/evento-equipos")
public class EventoEquipoResource {

    private static final Logger LOG = LoggerFactory.getLogger(EventoEquipoResource.class);

    private static final String ENTITY_NAME = "eventoEquipo";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EventoEquipoService eventoEquipoService;

    private final EventoEquipoRepository eventoEquipoRepository;

    private final EventoEquipoQueryService eventoEquipoQueryService;

    public EventoEquipoResource(
        EventoEquipoService eventoEquipoService,
        EventoEquipoRepository eventoEquipoRepository,
        EventoEquipoQueryService eventoEquipoQueryService
    ) {
        this.eventoEquipoService = eventoEquipoService;
        this.eventoEquipoRepository = eventoEquipoRepository;
        this.eventoEquipoQueryService = eventoEquipoQueryService;
    }

    /**
     * {@code POST  /evento-equipos} : Create a new eventoEquipo.
     *
     * @param eventoEquipoDTO the eventoEquipoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eventoEquipoDTO, or with status {@code 400 (Bad Request)} if the eventoEquipo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<EventoEquipoDTO> createEventoEquipo(@Valid @RequestBody EventoEquipoDTO eventoEquipoDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save EventoEquipo : {}", eventoEquipoDTO);
        if (eventoEquipoDTO.getId() != null) {
            throw new BadRequestAlertException("A new eventoEquipo cannot already have an ID", ENTITY_NAME, "idexists");
        }
        eventoEquipoDTO = eventoEquipoService.save(eventoEquipoDTO);
        return ResponseEntity.created(new URI("/api/evento-equipos/" + eventoEquipoDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, eventoEquipoDTO.getId().toString()))
            .body(eventoEquipoDTO);
    }

    /**
     * {@code PUT  /evento-equipos/:id} : Updates an existing eventoEquipo.
     *
     * @param id the id of the eventoEquipoDTO to save.
     * @param eventoEquipoDTO the eventoEquipoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventoEquipoDTO,
     * or with status {@code 400 (Bad Request)} if the eventoEquipoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eventoEquipoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventoEquipoDTO> updateEventoEquipo(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EventoEquipoDTO eventoEquipoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update EventoEquipo : {}, {}", id, eventoEquipoDTO);
        if (eventoEquipoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventoEquipoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventoEquipoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        eventoEquipoDTO = eventoEquipoService.update(eventoEquipoDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eventoEquipoDTO.getId().toString()))
            .body(eventoEquipoDTO);
    }

    /**
     * {@code PATCH  /evento-equipos/:id} : Partial updates given fields of an existing eventoEquipo, field will ignore if it is null
     *
     * @param id the id of the eventoEquipoDTO to save.
     * @param eventoEquipoDTO the eventoEquipoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventoEquipoDTO,
     * or with status {@code 400 (Bad Request)} if the eventoEquipoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the eventoEquipoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the eventoEquipoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EventoEquipoDTO> partialUpdateEventoEquipo(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EventoEquipoDTO eventoEquipoDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update EventoEquipo partially : {}, {}", id, eventoEquipoDTO);
        if (eventoEquipoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventoEquipoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventoEquipoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EventoEquipoDTO> result = eventoEquipoService.partialUpdate(eventoEquipoDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eventoEquipoDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /evento-equipos} : get all the eventoEquipos.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eventoEquipos in body.
     */
    @GetMapping("")
    public ResponseEntity<List<EventoEquipoDTO>> getAllEventoEquipos(
        EventoEquipoCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get EventoEquipos by criteria: {}", criteria);

        Page<EventoEquipoDTO> page = eventoEquipoQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /evento-equipos/count} : count all the eventoEquipos.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countEventoEquipos(EventoEquipoCriteria criteria) {
        LOG.debug("REST request to count EventoEquipos by criteria: {}", criteria);
        return ResponseEntity.ok().body(eventoEquipoQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /evento-equipos/:id} : get the "id" eventoEquipo.
     *
     * @param id the id of the eventoEquipoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eventoEquipoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventoEquipoDTO> getEventoEquipo(@PathVariable("id") Long id) {
        LOG.debug("REST request to get EventoEquipo : {}", id);
        Optional<EventoEquipoDTO> eventoEquipoDTO = eventoEquipoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(eventoEquipoDTO);
    }

    /**
     * {@code DELETE  /evento-equipos/:id} : delete the "id" eventoEquipo.
     *
     * @param id the id of the eventoEquipoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventoEquipo(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete EventoEquipo : {}", id);
        eventoEquipoService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
