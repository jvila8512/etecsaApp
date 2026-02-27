package com.etecsa.service;

import com.etecsa.domain.*; // for static metamodels
import com.etecsa.domain.Especialidad;
import com.etecsa.repository.EspecialidadRepository;
import com.etecsa.service.criteria.EspecialidadCriteria;
import com.etecsa.service.dto.EspecialidadDTO;
import com.etecsa.service.mapper.EspecialidadMapper;
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
 * Service for executing complex queries for {@link Especialidad} entities in the database.
 * The main input is a {@link EspecialidadCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link EspecialidadDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class EspecialidadQueryService extends QueryService<Especialidad> {

    private static final Logger LOG = LoggerFactory.getLogger(EspecialidadQueryService.class);

    private final EspecialidadRepository especialidadRepository;

    private final EspecialidadMapper especialidadMapper;

    public EspecialidadQueryService(EspecialidadRepository especialidadRepository, EspecialidadMapper especialidadMapper) {
        this.especialidadRepository = especialidadRepository;
        this.especialidadMapper = especialidadMapper;
    }

    /**
     * Return a {@link Page} of {@link EspecialidadDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<EspecialidadDTO> findByCriteria(EspecialidadCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Especialidad> specification = createSpecification(criteria);
        return especialidadRepository.findAll(specification, page).map(especialidadMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(EspecialidadCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Especialidad> specification = createSpecification(criteria);
        return especialidadRepository.count(specification);
    }

    /**
     * Function to convert {@link EspecialidadCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Especialidad> createSpecification(EspecialidadCriteria criteria) {
        Specification<Especialidad> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), Especialidad_.id),
                buildStringSpecification(criteria.getNombre(), Especialidad_.nombre),
                buildStringSpecification(criteria.getCodigo(), Especialidad_.codigo),
                buildStringSpecification(criteria.getDescripcionTecnica(), Especialidad_.descripcionTecnica),
                buildSpecification(criteria.getEquiposId(), root -> root.join(Especialidad_.equipos, JoinType.LEFT).get(Equipo_.id))
            );
        }
        return specification;
    }
}
