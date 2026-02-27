package com.etecsa.service;

import com.etecsa.domain.*; // for static metamodels
import com.etecsa.domain.EventoPlantilla;
import com.etecsa.repository.EventoPlantillaRepository;
import com.etecsa.service.criteria.EventoPlantillaCriteria;
import com.etecsa.service.dto.EventoPlantillaDTO;
import com.etecsa.service.mapper.EventoPlantillaMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link EventoPlantilla} entities in the database.
 * The main input is a {@link EventoPlantillaCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link EventoPlantillaDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class EventoPlantillaQueryService extends QueryService<EventoPlantilla> {

    private static final Logger LOG = LoggerFactory.getLogger(EventoPlantillaQueryService.class);

    private final EventoPlantillaRepository eventoPlantillaRepository;

    private final EventoPlantillaMapper eventoPlantillaMapper;

    public EventoPlantillaQueryService(EventoPlantillaRepository eventoPlantillaRepository, EventoPlantillaMapper eventoPlantillaMapper) {
        this.eventoPlantillaRepository = eventoPlantillaRepository;
        this.eventoPlantillaMapper = eventoPlantillaMapper;
    }

    /**
     * Return a {@link Page} of {@link EventoPlantillaDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<EventoPlantillaDTO> findByCriteria(EventoPlantillaCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<EventoPlantilla> specification = createSpecification(criteria);
        return eventoPlantillaRepository.findAll(specification, page).map(eventoPlantillaMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(EventoPlantillaCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<EventoPlantilla> specification = createSpecification(criteria);
        return eventoPlantillaRepository.count(specification);
    }

    /**
     * Function to convert {@link EventoPlantillaCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<EventoPlantilla> createSpecification(EventoPlantillaCriteria criteria) {
        Specification<EventoPlantilla> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), EventoPlantilla_.id),
                buildStringSpecification(criteria.getNombre(), EventoPlantilla_.nombre),
                buildStringSpecification(criteria.getDescripcion(), EventoPlantilla_.descripcion),
                buildRangeSpecification(criteria.getScalingFactor(), EventoPlantilla_.scalingFactor),
                buildStringSpecification(criteria.getUnidadMedida(), EventoPlantilla_.unidadMedida),
                buildStringSpecification(criteria.getFuncionLectura(), EventoPlantilla_.funcionLectura),
                buildStringSpecification(criteria.getFuncionEscritura(), EventoPlantilla_.funcionEscritura),
                buildSpecification(criteria.getEspecialidadId(), root ->
                    root.join(EventoPlantilla_.especialidad, JoinType.LEFT).get(Especialidad_.id)
                )
            );
        }
        return specification;
    }
}
