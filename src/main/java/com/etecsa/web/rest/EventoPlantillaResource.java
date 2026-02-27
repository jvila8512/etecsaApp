package com.etecsa.web.rest;

import com.etecsa.repository.EventoPlantillaRepository;
import com.etecsa.service.EventoPlantillaQueryService;
import com.etecsa.service.EventoPlantillaService;
import com.etecsa.service.criteria.EventoPlantillaCriteria;
import com.etecsa.service.dto.EventoPlantillaDTO;
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
 * REST controller for managing {@link com.etecsa.domain.EventoPlantilla}.
 */
@RestController
@RequestMapping("/api/evento-plantillas")
public class EventoPlantillaResource {

    private static final Logger LOG = LoggerFactory.getLogger(EventoPlantillaResource.class);

    private static final String ENTITY_NAME = "eventoPlantilla";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EventoPlantillaService eventoPlantillaService;

    private final EventoPlantillaRepository eventoPlantillaRepository;

    private final EventoPlantillaQueryService eventoPlantillaQueryService;

    public EventoPlantillaResource(
        EventoPlantillaService eventoPlantillaService,
        EventoPlantillaRepository eventoPlantillaRepository,
        EventoPlantillaQueryService eventoPlantillaQueryService
    ) {
        this.eventoPlantillaService = eventoPlantillaService;
        this.eventoPlantillaRepository = eventoPlantillaRepository;
        this.eventoPlantillaQueryService = eventoPlantillaQueryService;
    }

    /**
     * {@code POST  /evento-plantillas} : Create a new eventoPlantilla.
     *
     * @param eventoPlantillaDTO the eventoPlantillaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eventoPlantillaDTO, or with status {@code 400 (Bad Request)} if the eventoPlantilla has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<EventoPlantillaDTO> createEventoPlantilla(@Valid @RequestBody EventoPlantillaDTO eventoPlantillaDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save EventoPlantilla : {}", eventoPlantillaDTO);
        if (eventoPlantillaDTO.getId() != null) {
            throw new BadRequestAlertException("A new eventoPlantilla cannot already have an ID", ENTITY_NAME, "idexists");
        }
        eventoPlantillaDTO = eventoPlantillaService.save(eventoPlantillaDTO);
        return ResponseEntity.created(new URI("/api/evento-plantillas/" + eventoPlantillaDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, eventoPlantillaDTO.getId().toString()))
            .body(eventoPlantillaDTO);
    }

    /**
     * {@code PUT  /evento-plantillas/:id} : Updates an existing eventoPlantilla.
     *
     * @param id the id of the eventoPlantillaDTO to save.
     * @param eventoPlantillaDTO the eventoPlantillaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventoPlantillaDTO,
     * or with status {@code 400 (Bad Request)} if the eventoPlantillaDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eventoPlantillaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventoPlantillaDTO> updateEventoPlantilla(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EventoPlantillaDTO eventoPlantillaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update EventoPlantilla : {}, {}", id, eventoPlantillaDTO);
        if (eventoPlantillaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventoPlantillaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventoPlantillaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        eventoPlantillaDTO = eventoPlantillaService.update(eventoPlantillaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eventoPlantillaDTO.getId().toString()))
            .body(eventoPlantillaDTO);
    }

    /**
     * {@code PATCH  /evento-plantillas/:id} : Partial updates given fields of an existing eventoPlantilla, field will ignore if it is null
     *
     * @param id the id of the eventoPlantillaDTO to save.
     * @param eventoPlantillaDTO the eventoPlantillaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eventoPlantillaDTO,
     * or with status {@code 400 (Bad Request)} if the eventoPlantillaDTO is not valid,
     * or with status {@code 404 (Not Found)} if the eventoPlantillaDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the eventoPlantillaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EventoPlantillaDTO> partialUpdateEventoPlantilla(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EventoPlantillaDTO eventoPlantillaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update EventoPlantilla partially : {}, {}", id, eventoPlantillaDTO);
        if (eventoPlantillaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eventoPlantillaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventoPlantillaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EventoPlantillaDTO> result = eventoPlantillaService.partialUpdate(eventoPlantillaDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eventoPlantillaDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /evento-plantillas} : get all the eventoPlantillas.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eventoPlantillas in body.
     */
    @GetMapping("")
    public ResponseEntity<List<EventoPlantillaDTO>> getAllEventoPlantillas(
        EventoPlantillaCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get EventoPlantillas by criteria: {}", criteria);

        Page<EventoPlantillaDTO> page = eventoPlantillaQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /evento-plantillas/count} : count all the eventoPlantillas.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countEventoPlantillas(EventoPlantillaCriteria criteria) {
        LOG.debug("REST request to count EventoPlantillas by criteria: {}", criteria);
        return ResponseEntity.ok().body(eventoPlantillaQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /evento-plantillas/:id} : get the "id" eventoPlantilla.
     *
     * @param id the id of the eventoPlantillaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eventoPlantillaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventoPlantillaDTO> getEventoPlantilla(@PathVariable("id") Long id) {
        LOG.debug("REST request to get EventoPlantilla : {}", id);
        Optional<EventoPlantillaDTO> eventoPlantillaDTO = eventoPlantillaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(eventoPlantillaDTO);
    }

    /**
     * {@code DELETE  /evento-plantillas/:id} : delete the "id" eventoPlantilla.
     *
     * @param id the id of the eventoPlantillaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventoPlantilla(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete EventoPlantilla : {}", id);
        eventoPlantillaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
