package com.etecsa.service;

import com.etecsa.domain.*; // for static metamodels
import com.etecsa.domain.Alarma;
import com.etecsa.repository.AlarmaRepository;
import com.etecsa.service.criteria.AlarmaCriteria;
import com.etecsa.service.dto.AlarmaDTO;
import com.etecsa.service.mapper.AlarmaMapper;
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
 * Service for executing complex queries for {@link Alarma} entities in the database.
 * The main input is a {@link AlarmaCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link AlarmaDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AlarmaQueryService extends QueryService<Alarma> {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmaQueryService.class);

    private final AlarmaRepository alarmaRepository;

    private final AlarmaMapper alarmaMapper;

    public AlarmaQueryService(AlarmaRepository alarmaRepository, AlarmaMapper alarmaMapper) {
        this.alarmaRepository = alarmaRepository;
        this.alarmaMapper = alarmaMapper;
    }

    /**
     * Return a {@link Page} of {@link AlarmaDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AlarmaDTO> findByCriteria(AlarmaCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Alarma> specification = createSpecification(criteria);
        return alarmaRepository.findAll(specification, page).map(alarmaMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AlarmaCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Alarma> specification = createSpecification(criteria);
        return alarmaRepository.count(specification);
    }

    /**
     * Function to convert {@link AlarmaCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Alarma> createSpecification(AlarmaCriteria criteria) {
        Specification<Alarma> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), Alarma_.id),
                buildStringSpecification(criteria.getDescripcion(), Alarma_.descripcion),
                buildRangeSpecification(criteria.getActivatedAt(), Alarma_.activatedAt),
                buildRangeSpecification(criteria.getDeactivatedAt(), Alarma_.deactivatedAt),
                buildSpecification(criteria.getSeveridad(), Alarma_.severidad),
                buildSpecification(criteria.getEstado(), Alarma_.estado),
                buildStringSpecification(criteria.getMensajeUsuario(), Alarma_.mensajeUsuario),
                buildSpecification(criteria.getEventoId(), root -> root.join(Alarma_.evento, JoinType.LEFT).get(EventoEquipo_.id)),
                buildSpecification(criteria.getAcknowledgedById(), root -> root.join(Alarma_.acknowledgedBy, JoinType.LEFT).get(User_.id))
            );
        }
        return specification;
    }
}
