package com.etecsa.web.rest;

import com.etecsa.repository.AlarmaRepository;
import com.etecsa.service.AlarmaQueryService;
import com.etecsa.service.AlarmaService;
import com.etecsa.service.criteria.AlarmaCriteria;
import com.etecsa.service.dto.AlarmaDTO;
import com.etecsa.service.dto.AlarmaDeteccionDTO;
import com.etecsa.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
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
 * REST controller for managing {@link com.etecsa.domain.Alarma}.
 */
@RestController
@RequestMapping("/api/alarmas")
public class AlarmaResource {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmaResource.class);

    private static final String ENTITY_NAME = "alarma";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AlarmaService alarmaService;

    private final AlarmaRepository alarmaRepository;

    private final AlarmaQueryService alarmaQueryService;

    public AlarmaResource(AlarmaService alarmaService, AlarmaRepository alarmaRepository, AlarmaQueryService alarmaQueryService) {
        this.alarmaService = alarmaService;
        this.alarmaRepository = alarmaRepository;
        this.alarmaQueryService = alarmaQueryService;
    }

    /**
     * {@code POST  /alarmas} : Create a new alarma.
     *
     * @param alarmaDTO the alarmaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new alarmaDTO, or with status {@code 400 (Bad Request)} if the alarma has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AlarmaDTO> createAlarma(@Valid @RequestBody AlarmaDTO alarmaDTO) throws URISyntaxException {
        LOG.debug("REST request to save Alarma : {}", alarmaDTO);
        if (alarmaDTO.getId() != null) {
            throw new BadRequestAlertException("A new alarma cannot already have an ID", ENTITY_NAME, "idexists");
        }
        alarmaDTO = alarmaService.save(alarmaDTO);
        return ResponseEntity.created(new URI("/api/alarmas/" + alarmaDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, alarmaDTO.getId().toString()))
            .body(alarmaDTO);
    }

    /**
     * {@code PUT  /alarmas/:id} : Updates an existing alarma.
     *
     * @param id the id of the alarmaDTO to save.
     * @param alarmaDTO the alarmaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alarmaDTO,
     * or with status {@code 400 (Bad Request)} if the alarmaDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the alarmaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AlarmaDTO> updateAlarma(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AlarmaDTO alarmaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Alarma : {}, {}", id, alarmaDTO);
        if (alarmaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alarmaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alarmaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        alarmaDTO = alarmaService.update(alarmaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, alarmaDTO.getId().toString()))
            .body(alarmaDTO);
    }

    /**
     * {@code PATCH  /alarmas/:id} : Partial updates given fields of an existing alarma, field will ignore if it is null
     *
     * @param id the id of the alarmaDTO to save.
     * @param alarmaDTO the alarmaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated alarmaDTO,
     * or with status {@code 400 (Bad Request)} if the alarmaDTO is not valid,
     * or with status {@code 404 (Not Found)} if the alarmaDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the alarmaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AlarmaDTO> partialUpdateAlarma(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AlarmaDTO alarmaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Alarma partially : {}, {}", id, alarmaDTO);
        if (alarmaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, alarmaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!alarmaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AlarmaDTO> result = alarmaService.partialUpdate(alarmaDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, alarmaDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /alarmas} : get all the alarmas.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of alarmas in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AlarmaDTO>> getAllAlarmas(
        AlarmaCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Alarmas by criteria: {}", criteria);

        Page<AlarmaDTO> page = alarmaQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /alarmas/count} : count all the alarmas.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAlarmas(AlarmaCriteria criteria) {
        LOG.debug("REST request to count Alarmas by criteria: {}", criteria);
        return ResponseEntity.ok().body(alarmaQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /alarmas/:id} : get the "id" alarma.
     *
     * @param id the id of the alarmaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the alarmaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlarmaDTO> getAlarma(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Alarma : {}", id);
        Optional<AlarmaDTO> alarmaDTO = alarmaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(alarmaDTO);
    }

    /**
     * {@code DELETE  /alarmas/:id} : delete the "id" alarma.
     *
     * @param id the id of the alarmaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlarma(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Alarma : {}", id);
        alarmaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code POST  /alarmas/procesar-deteccion} : Procesa una detección de alarma.
     * Endpoint inteligente que:
     * - Si esAlarma=true Y no existe → crea nueva ACTIVA
     * - Si esAlarma=true Y ya existe → no hace nada
     * - Si esAlarma=false Y existe → FINALIZA
     * - Si esAlarma=false Y no existe → no hace nada
     *
     * @param deteccion los datos de la detección.
     * @return la nueva alarma creada, o 204 si no se creó/modificó nada.
     */
    @PostMapping("/procesar-deteccion")
    public ResponseEntity<AlarmaDTO> procesarDeteccion(@RequestBody AlarmaDeteccionDTO deteccion) {
        LOG.debug("REST request to procesar deteccion: {}", deteccion);
        AlarmaDTO result = alarmaService.procesarDeteccion(deteccion);

        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code POST  /alarmas/{id}/reconocer} : Reconoce una alarma (ACTIVA → RECONOCIDA).
     *
     * @param id el id de la alarma.
     * @param principal el usuario autenticado.
     * @return la alarma actualizada.
     */
    @PostMapping("/{id}/reconocer")
    public ResponseEntity<AlarmaDTO> reconocerAlarma(@PathVariable("id") Long id, Principal principal) {
        LOG.debug("REST request to reconocer alarma: {}", id);
        String username = principal != null ? principal.getName() : null;
        Optional<AlarmaDTO> result = alarmaService.reconocerAlarma(id, username);

        return ResponseUtil.wrapOrNotFound(result);
    }

    /**
     * {@code POST  /alarmas/{id}/finalizar} : Finaliza una alarma manualmente.
     *
     * @param id el id de la alarma.
     * @return la alarma actualizada.
     */
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<AlarmaDTO> finalizarAlarma(@PathVariable("id") Long id) {
        LOG.debug("REST request to finalizar alarma: {}", id);
        Optional<AlarmaDTO> result = alarmaService.finalizarAlarma(id);

        return ResponseUtil.wrapOrNotFound(result);
    }

    /**
     * {@code GET  /alarmas/activas/evento/{eventoId}} : Obtiene alarmas no finalizadas para un evento.
     *
     * @param eventoId el id del evento.
     * @return lista de alarmas activas/reconocidas.
     */
    @GetMapping("/activas/evento/{eventoId}")
    public ResponseEntity<List<AlarmaDTO>> getAlarmasActivasParaEvento(@PathVariable("eventoId") Long eventoId) {
        LOG.debug("REST request to get alarmas activas para evento: {}", eventoId);
        List<AlarmaDTO> result = alarmaService.obtenerAlarmasActivasParaEvento(eventoId);

        return ResponseEntity.ok(result);
    }
}
