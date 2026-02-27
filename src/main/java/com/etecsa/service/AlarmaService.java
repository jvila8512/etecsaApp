package com.etecsa.service;

import com.etecsa.domain.Alarma;
import com.etecsa.repository.AlarmaRepository;
import com.etecsa.service.dto.AlarmaDTO;
import com.etecsa.service.mapper.AlarmaMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.etecsa.domain.Alarma}.
 */
@Service
@Transactional
public class AlarmaService {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmaService.class);

    private final AlarmaRepository alarmaRepository;

    private final AlarmaMapper alarmaMapper;

    public AlarmaService(AlarmaRepository alarmaRepository, AlarmaMapper alarmaMapper) {
        this.alarmaRepository = alarmaRepository;
        this.alarmaMapper = alarmaMapper;
    }

    /**
     * Save a alarma.
     *
     * @param alarmaDTO the entity to save.
     * @return the persisted entity.
     */
    public AlarmaDTO save(AlarmaDTO alarmaDTO) {
        LOG.debug("Request to save Alarma : {}", alarmaDTO);
        Alarma alarma = alarmaMapper.toEntity(alarmaDTO);
        alarma = alarmaRepository.save(alarma);
        return alarmaMapper.toDto(alarma);
    }

    /**
     * Update a alarma.
     *
     * @param alarmaDTO the entity to save.
     * @return the persisted entity.
     */
    public AlarmaDTO update(AlarmaDTO alarmaDTO) {
        LOG.debug("Request to update Alarma : {}", alarmaDTO);
        Alarma alarma = alarmaMapper.toEntity(alarmaDTO);
        alarma = alarmaRepository.save(alarma);
        return alarmaMapper.toDto(alarma);
    }

    /**
     * Partially update a alarma.
     *
     * @param alarmaDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AlarmaDTO> partialUpdate(AlarmaDTO alarmaDTO) {
        LOG.debug("Request to partially update Alarma : {}", alarmaDTO);

        return alarmaRepository
            .findById(alarmaDTO.getId())
            .map(existingAlarma -> {
                alarmaMapper.partialUpdate(existingAlarma, alarmaDTO);

                return existingAlarma;
            })
            .map(alarmaRepository::save)
            .map(alarmaMapper::toDto);
    }

    /**
     * Get all the alarmas with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<AlarmaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return alarmaRepository.findAllWithEagerRelationships(pageable).map(alarmaMapper::toDto);
    }

    /**
     * Get one alarma by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AlarmaDTO> findOne(Long id) {
        LOG.debug("Request to get Alarma : {}", id);
        return alarmaRepository.findOneWithEagerRelationships(id).map(alarmaMapper::toDto);
    }

    /**
     * Delete the alarma by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Alarma : {}", id);
        alarmaRepository.deleteById(id);
    }
}
