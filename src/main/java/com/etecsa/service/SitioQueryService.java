package com.etecsa.service;

import com.etecsa.domain.*; // for static metamodels
import com.etecsa.domain.Sitio;
import com.etecsa.repository.SitioRepository;
import com.etecsa.service.criteria.SitioCriteria;
import com.etecsa.service.dto.SitioDTO;
import com.etecsa.service.mapper.SitioMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Sitio} entities in the database.
 * The main input is a {@link SitioCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link SitioDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class SitioQueryService extends QueryService<Sitio> {

    private static final Logger LOG = LoggerFactory.getLogger(SitioQueryService.class);

    private final SitioRepository sitioRepository;

    private final SitioMapper sitioMapper;

    public SitioQueryService(SitioRepository sitioRepository, SitioMapper sitioMapper) {
        this.sitioRepository = sitioRepository;
        this.sitioMapper = sitioMapper;
    }

    /**
     * Return a {@link Page} of {@link SitioDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<SitioDTO> findByCriteria(SitioCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Sitio> specification = createSpecification(criteria);
        return sitioRepository.findAll(specification, page).map(sitioMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SitioCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Sitio> specification = createSpecification(criteria);
        return sitioRepository.count(specification);
    }

    /**
     * Function to convert {@link SitioCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Sitio> createSpecification(SitioCriteria criteria) {
        Specification<Sitio> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), Sitio_.id),
                buildStringSpecification(criteria.getNombre(), Sitio_.nombre),
                buildStringSpecification(criteria.getCodigo(), Sitio_.codigo),
                buildStringSpecification(criteria.getUbicacion(), Sitio_.ubicacion),
                buildRangeSpecification(criteria.getFechaRegistro(), Sitio_.fechaRegistro)
            );
        }
        return specification;
    }
}
