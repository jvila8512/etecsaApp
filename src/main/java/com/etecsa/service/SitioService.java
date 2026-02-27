package com.etecsa.service;

import com.etecsa.domain.Sitio;
import com.etecsa.repository.SitioRepository;
import com.etecsa.service.dto.SitioDTO;
import com.etecsa.service.mapper.SitioMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.etecsa.domain.Sitio}.
 */
@Service
@Transactional
public class SitioService {

    private static final Logger LOG = LoggerFactory.getLogger(SitioService.class);

    private final SitioRepository sitioRepository;

    private final SitioMapper sitioMapper;

    public SitioService(SitioRepository sitioRepository, SitioMapper sitioMapper) {
        this.sitioRepository = sitioRepository;
        this.sitioMapper = sitioMapper;
    }

    /**
     * Save a sitio.
     *
     * @param sitioDTO the entity to save.
     * @return the persisted entity.
     */
    public SitioDTO save(SitioDTO sitioDTO) {
        LOG.debug("Request to save Sitio : {}", sitioDTO);
        Sitio sitio = sitioMapper.toEntity(sitioDTO);
        sitio = sitioRepository.save(sitio);
        return sitioMapper.toDto(sitio);
    }

    /**
     * Update a sitio.
     *
     * @param sitioDTO the entity to save.
     * @return the persisted entity.
     */
    public SitioDTO update(SitioDTO sitioDTO) {
        LOG.debug("Request to update Sitio : {}", sitioDTO);
        Sitio sitio = sitioMapper.toEntity(sitioDTO);
        sitio = sitioRepository.save(sitio);
        return sitioMapper.toDto(sitio);
    }

    /**
     * Partially update a sitio.
     *
     * @param sitioDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SitioDTO> partialUpdate(SitioDTO sitioDTO) {
        LOG.debug("Request to partially update Sitio : {}", sitioDTO);

        return sitioRepository
            .findById(sitioDTO.getId())
            .map(existingSitio -> {
                sitioMapper.partialUpdate(existingSitio, sitioDTO);

                return existingSitio;
            })
            .map(sitioRepository::save)
            .map(sitioMapper::toDto);
    }

    /**
     * Get one sitio by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SitioDTO> findOne(Long id) {
        LOG.debug("Request to get Sitio : {}", id);
        return sitioRepository.findById(id).map(sitioMapper::toDto);
    }

    /**
     * Delete the sitio by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Sitio : {}", id);
        sitioRepository.deleteById(id);
    }
}
