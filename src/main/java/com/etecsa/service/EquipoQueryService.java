package com.etecsa.service;

import com.etecsa.domain.*; // for static metamodels
import com.etecsa.domain.Equipo;
import com.etecsa.repository.EquipoRepository;
import com.etecsa.service.criteria.EquipoCriteria;
import com.etecsa.service.dto.EquipoDTO;
import com.etecsa.service.mapper.EquipoMapper;
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
 * Service for executing complex queries for {@link Equipo} entities in the database.
 * The main input is a {@link EquipoCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link EquipoDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class EquipoQueryService extends QueryService<Equipo> {

    private static final Logger LOG = LoggerFactory.getLogger(EquipoQueryService.class);

    private final EquipoRepository equipoRepository;

    private final EquipoMapper equipoMapper;

    public EquipoQueryService(EquipoRepository equipoRepository, EquipoMapper equipoMapper) {
        this.equipoRepository = equipoRepository;
        this.equipoMapper = equipoMapper;
    }

    /**
     * Return a {@link Page} of {@link EquipoDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<EquipoDTO> findByCriteria(EquipoCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Equipo> specification = createSpecification(criteria);
        return equipoRepository.fetchBagRelationships(equipoRepository.findAll(specification, page)).map(equipoMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(EquipoCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Equipo> specification = createSpecification(criteria);
        return equipoRepository.count(specification);
    }

    /**
     * Function to convert {@link EquipoCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Equipo> createSpecification(EquipoCriteria criteria) {
        Specification<Equipo> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), Equipo_.id),
                buildStringSpecification(criteria.getNombre(), Equipo_.nombre),
                buildStringSpecification(criteria.getDireccionIp(), Equipo_.direccionIp),
                buildRangeSpecification(criteria.getModbusSlaveId(), Equipo_.modbusSlaveId),
                buildStringSpecification(criteria.getModelo(), Equipo_.modelo),
                buildStringSpecification(criteria.getFirmwareVersion(), Equipo_.firmwareVersion),
                buildSpecification(criteria.getEstado(), Equipo_.estado),
                buildRangeSpecification(criteria.getUltimoHeartbeat(), Equipo_.ultimoHeartbeat),
                buildSpecification(criteria.getSitioId(), root -> root.join(Equipo_.sitio, JoinType.LEFT).get(Sitio_.id)),
                buildSpecification(criteria.getEspecialidadesId(), root ->
                    root.join(Equipo_.especialidades, JoinType.LEFT).get(Especialidad_.id)
                )
            );
        }
        return specification;
    }
}
