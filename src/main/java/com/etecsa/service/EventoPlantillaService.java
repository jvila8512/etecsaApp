package com.etecsa.service;

import com.etecsa.domain.EventoPlantilla;
import com.etecsa.repository.EventoPlantillaRepository;
import com.etecsa.service.dto.EventoPlantillaDTO;
import com.etecsa.service.mapper.EventoPlantillaMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.etecsa.domain.EventoPlantilla}.
 */
@Service
@Transactional
public class EventoPlantillaService {

    private static final Logger LOG = LoggerFactory.getLogger(EventoPlantillaService.class);

    private final EventoPlantillaRepository eventoPlantillaRepository;

    private final EventoPlantillaMapper eventoPlantillaMapper;

    public EventoPlantillaService(EventoPlantillaRepository eventoPlantillaRepository, EventoPlantillaMapper eventoPlantillaMapper) {
        this.eventoPlantillaRepository = eventoPlantillaRepository;
        this.eventoPlantillaMapper = eventoPlantillaMapper;
    }

    /**
     * Save a eventoPlantilla.
     *
     * @param eventoPlantillaDTO the entity to save.
     * @return the persisted entity.
     */
    public EventoPlantillaDTO save(EventoPlantillaDTO eventoPlantillaDTO) {
        LOG.debug("Request to save EventoPlantilla : {}", eventoPlantillaDTO);
        EventoPlantilla eventoPlantilla = eventoPlantillaMapper.toEntity(eventoPlantillaDTO);
        eventoPlantilla = eventoPlantillaRepository.save(eventoPlantilla);
        return eventoPlantillaMapper.toDto(eventoPlantilla);
    }

    /**
     * Update a eventoPlantilla.
     *
     * @param eventoPlantillaDTO the entity to save.
     * @return the persisted entity.
     */
    public EventoPlantillaDTO update(EventoPlantillaDTO eventoPlantillaDTO) {
        LOG.debug("Request to update EventoPlantilla : {}", eventoPlantillaDTO);
        EventoPlantilla eventoPlantilla = eventoPlantillaMapper.toEntity(eventoPlantillaDTO);
        eventoPlantilla = eventoPlantillaRepository.save(eventoPlantilla);
        return eventoPlantillaMapper.toDto(eventoPlantilla);
    }

    /**
     * Partially update a eventoPlantilla.
     *
     * @param eventoPlantillaDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<EventoPlantillaDTO> partialUpdate(EventoPlantillaDTO eventoPlantillaDTO) {
        LOG.debug("Request to partially update EventoPlantilla : {}", eventoPlantillaDTO);

        return eventoPlantillaRepository
            .findById(eventoPlantillaDTO.getId())
            .map(existingEventoPlantilla -> {
                eventoPlantillaMapper.partialUpdate(existingEventoPlantilla, eventoPlantillaDTO);

                return existingEventoPlantilla;
            })
            .map(eventoPlantillaRepository::save)
            .map(eventoPlantillaMapper::toDto);
    }

    /**
     * Get all the eventoPlantillas with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<EventoPlantillaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return eventoPlantillaRepository.findAllWithEagerRelationships(pageable).map(eventoPlantillaMapper::toDto);
    }

    /**
     * Get one eventoPlantilla by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<EventoPlantillaDTO> findOne(Long id) {
        LOG.debug("Request to get EventoPlantilla : {}", id);
        return eventoPlantillaRepository.findOneWithEagerRelationships(id).map(eventoPlantillaMapper::toDto);
    }

    /**
     * Delete the eventoPlantilla by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete EventoPlantilla : {}", id);
        eventoPlantillaRepository.deleteById(id);
    }
}
