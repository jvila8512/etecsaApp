package com.etecsa.service;

import com.etecsa.domain.EventoEquipo;
import com.etecsa.repository.EventoEquipoRepository;
import com.etecsa.service.dto.EventoEquipoDTO;
import com.etecsa.service.mapper.EventoEquipoMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.etecsa.domain.EventoEquipo}.
 */
@Service
@Transactional
public class EventoEquipoService {

    private static final Logger LOG = LoggerFactory.getLogger(EventoEquipoService.class);

    private final EventoEquipoRepository eventoEquipoRepository;

    private final EventoEquipoMapper eventoEquipoMapper;

    public EventoEquipoService(EventoEquipoRepository eventoEquipoRepository, EventoEquipoMapper eventoEquipoMapper) {
        this.eventoEquipoRepository = eventoEquipoRepository;
        this.eventoEquipoMapper = eventoEquipoMapper;
    }

    /**
     * Save a eventoEquipo.
     *
     * @param eventoEquipoDTO the entity to save.
     * @return the persisted entity.
     */
    public EventoEquipoDTO save(EventoEquipoDTO eventoEquipoDTO) {
        LOG.debug("Request to save EventoEquipo : {}", eventoEquipoDTO);
        EventoEquipo eventoEquipo = eventoEquipoMapper.toEntity(eventoEquipoDTO);
        eventoEquipo = eventoEquipoRepository.save(eventoEquipo);
        return eventoEquipoMapper.toDto(eventoEquipo);
    }

    /**
     * Update a eventoEquipo.
     *
     * @param eventoEquipoDTO the entity to save.
     * @return the persisted entity.
     */
    public EventoEquipoDTO update(EventoEquipoDTO eventoEquipoDTO) {
        LOG.debug("Request to update EventoEquipo : {}", eventoEquipoDTO);
        EventoEquipo eventoEquipo = eventoEquipoMapper.toEntity(eventoEquipoDTO);
        eventoEquipo = eventoEquipoRepository.save(eventoEquipo);
        return eventoEquipoMapper.toDto(eventoEquipo);
    }

    /**
     * Partially update a eventoEquipo.
     *
     * @param eventoEquipoDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<EventoEquipoDTO> partialUpdate(EventoEquipoDTO eventoEquipoDTO) {
        LOG.debug("Request to partially update EventoEquipo : {}", eventoEquipoDTO);

        return eventoEquipoRepository
            .findById(eventoEquipoDTO.getId())
            .map(existingEventoEquipo -> {
                eventoEquipoMapper.partialUpdate(existingEventoEquipo, eventoEquipoDTO);

                return existingEventoEquipo;
            })
            .map(eventoEquipoRepository::save)
            .map(eventoEquipoMapper::toDto);
    }

    /**
     * Get all the eventoEquipos with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<EventoEquipoDTO> findAllWithEagerRelationships(Pageable pageable) {
        return eventoEquipoRepository.findAllWithEagerRelationships(pageable).map(eventoEquipoMapper::toDto);
    }

    /**
     * Get one eventoEquipo by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<EventoEquipoDTO> findOne(Long id) {
        LOG.debug("Request to get EventoEquipo : {}", id);
        return eventoEquipoRepository.findOneWithEagerRelationships(id).map(eventoEquipoMapper::toDto);
    }

    /**
     * Delete the eventoEquipo by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete EventoEquipo : {}", id);
        eventoEquipoRepository.deleteById(id);
    }
}
