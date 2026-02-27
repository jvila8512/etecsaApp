package com.etecsa.web.rest;

import com.etecsa.repository.SitioRepository;
import com.etecsa.service.SitioQueryService;
import com.etecsa.service.SitioService;
import com.etecsa.service.criteria.SitioCriteria;
import com.etecsa.service.dto.SitioDTO;
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
 * REST controller for managing {@link com.etecsa.domain.Sitio}.
 */
@RestController
@RequestMapping("/api/sitios")
public class SitioResource {

    private static final Logger LOG = LoggerFactory.getLogger(SitioResource.class);

    private static final String ENTITY_NAME = "sitio";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SitioService sitioService;

    private final SitioRepository sitioRepository;

    private final SitioQueryService sitioQueryService;

    public SitioResource(SitioService sitioService, SitioRepository sitioRepository, SitioQueryService sitioQueryService) {
        this.sitioService = sitioService;
        this.sitioRepository = sitioRepository;
        this.sitioQueryService = sitioQueryService;
    }

    /**
     * {@code POST  /sitios} : Create a new sitio.
     *
     * @param sitioDTO the sitioDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new sitioDTO, or with status {@code 400 (Bad Request)} if the sitio has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SitioDTO> createSitio(@Valid @RequestBody SitioDTO sitioDTO) throws URISyntaxException {
        LOG.debug("REST request to save Sitio : {}", sitioDTO);
        if (sitioDTO.getId() != null) {
            throw new BadRequestAlertException("A new sitio cannot already have an ID", ENTITY_NAME, "idexists");
        }
        sitioDTO = sitioService.save(sitioDTO);
        return ResponseEntity.created(new URI("/api/sitios/" + sitioDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, sitioDTO.getId().toString()))
            .body(sitioDTO);
    }

    /**
     * {@code PUT  /sitios/:id} : Updates an existing sitio.
     *
     * @param id the id of the sitioDTO to save.
     * @param sitioDTO the sitioDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sitioDTO,
     * or with status {@code 400 (Bad Request)} if the sitioDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the sitioDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SitioDTO> updateSitio(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SitioDTO sitioDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Sitio : {}, {}", id, sitioDTO);
        if (sitioDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sitioDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!sitioRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        sitioDTO = sitioService.update(sitioDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, sitioDTO.getId().toString()))
            .body(sitioDTO);
    }

    /**
     * {@code PATCH  /sitios/:id} : Partial updates given fields of an existing sitio, field will ignore if it is null
     *
     * @param id the id of the sitioDTO to save.
     * @param sitioDTO the sitioDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sitioDTO,
     * or with status {@code 400 (Bad Request)} if the sitioDTO is not valid,
     * or with status {@code 404 (Not Found)} if the sitioDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the sitioDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SitioDTO> partialUpdateSitio(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SitioDTO sitioDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Sitio partially : {}, {}", id, sitioDTO);
        if (sitioDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sitioDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!sitioRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SitioDTO> result = sitioService.partialUpdate(sitioDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, sitioDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /sitios} : get all the sitios.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of sitios in body.
     */
    @GetMapping("")
    public ResponseEntity<List<SitioDTO>> getAllSitios(
        SitioCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Sitios by criteria: {}", criteria);

        Page<SitioDTO> page = sitioQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /sitios/count} : count all the sitios.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countSitios(SitioCriteria criteria) {
        LOG.debug("REST request to count Sitios by criteria: {}", criteria);
        return ResponseEntity.ok().body(sitioQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /sitios/:id} : get the "id" sitio.
     *
     * @param id the id of the sitioDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the sitioDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SitioDTO> getSitio(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Sitio : {}", id);
        Optional<SitioDTO> sitioDTO = sitioService.findOne(id);
        return ResponseUtil.wrapOrNotFound(sitioDTO);
    }

    /**
     * {@code DELETE  /sitios/:id} : delete the "id" sitio.
     *
     * @param id the id of the sitioDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSitio(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Sitio : {}", id);
        sitioService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
