package com.etecsa.service;

import com.etecsa.domain.Alarma;
import com.etecsa.domain.EventoEquipo;
import com.etecsa.domain.User;
import com.etecsa.domain.enumeration.EstadoAlarma;
import com.etecsa.domain.enumeration.Severidad;
import com.etecsa.repository.AlarmaRepository;
import com.etecsa.repository.EventoEquipoRepository;
import com.etecsa.repository.UserRepository;
import com.etecsa.service.alarma.AlarmaWebSocketService;
import com.etecsa.service.dto.AlarmaDTO;
import com.etecsa.service.dto.AlarmaDeteccionDTO;
import com.etecsa.service.mapper.AlarmaMapper;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
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
    private final EventoEquipoRepository eventoEquipoRepository;
    private final UserRepository userRepository;
    private final AlarmaMapper alarmaMapper;
    private final AlarmaWebSocketService alarmaWebSocketService;

    public AlarmaService(
        AlarmaRepository alarmaRepository,
        EventoEquipoRepository eventoEquipoRepository,
        UserRepository userRepository,
        AlarmaMapper alarmaMapper,
        AlarmaWebSocketService alarmaWebSocketService
    ) {
        this.alarmaRepository = alarmaRepository;
        this.eventoEquipoRepository = eventoEquipoRepository;
        this.userRepository = userRepository;
        this.alarmaMapper = alarmaMapper;
        this.alarmaWebSocketService = alarmaWebSocketService;
    }

    /**
     * Procesa una detección de alarma desde el frontend.
     * Lógica:
     * - Si esAlarma=true Y no existe alarma ACTIVA/RECONOCIDA → crear nueva ACTIVA
     * - Si esAlarma=true Y ya existe alarma ACTIVA/RECONOCIDA → no hacer nada
     * - Si esAlarma=false → NO hacer nada (el operador resuelve manualmente)
     */
    public AlarmaDTO procesarDeteccion(AlarmaDeteccionDTO deteccion) {
        LOG.info(">>> procesarDeteccion: evento={}, esAlarma={}", deteccion.getEventoId(), deteccion.getEsAlarma());

        List<EstadoAlarma> estadosActivos = Arrays.asList(EstadoAlarma.ACTIVA, EstadoAlarma.RECONOCIDA);

        if (deteccion.getEsAlarma() == null || !deteccion.getEsAlarma()) {
            LOG.info(">>> Valor normal (false), no se toman acciones");
            return null;
        }

        long countExistentes = alarmaRepository.countByEventoIdAndEstadoIn(deteccion.getEventoId(), estadosActivos);
        LOG.info(">>> Alarmas existentes (ACTIVA/RECONOCIDA): {}", countExistentes);

        if (countExistentes > 0) {
            LOG.info(">>> Ya existe alarma activa/reconocida, no se crea otra");
            return null;
        }

        Optional<EventoEquipo> eventoOpt = eventoEquipoRepository.findById(deteccion.getEventoId());
        if (eventoOpt.isEmpty()) {
            LOG.warn("Evento {} no encontrado", deteccion.getEventoId());
            return null;
        }

        EventoEquipo evento = eventoOpt.get();
        Alarma nueva = new Alarma();
        nueva.setEvento(evento);
        nueva.setDescripcion(deteccion.getDescripcion() != null ? deteccion.getDescripcion() : generarDescripcion(evento, deteccion));
        nueva.setSeveridad(deteccion.getSeveridad() != null ? Severidad.valueOf(deteccion.getSeveridad()) : Severidad.BAJA);
        nueva.setMensajeUsuario(deteccion.getMensajeUsuario());
        nueva.setEstado(EstadoAlarma.ACTIVA);
        nueva.setActivatedAt(ZonedDateTime.now());

        Alarma saved = alarmaRepository.save(nueva);
        LOG.info("Nueva alarma creada: {} para evento {}", saved.getId(), deteccion.getEventoId());

        AlarmaDTO dto = alarmaMapper.toDto(saved);
        alarmaWebSocketService.notificarNuevaAlarma(dto);

        return dto;
    }

    private String generarDescripcion(EventoEquipo evento, AlarmaDeteccionDTO deteccion) {
        String nombreVar = evento.getNombreVariable() != null ? evento.getNombreVariable() : "Variable";
        String severidad = deteccion.getSeveridad() != null ? deteccion.getSeveridad() : "BAJA";

        if (evento.getUmbralAlerta() != null) {
            return severidad + ": " + nombreVar + " > " + evento.getUmbralAlerta();
        }
        return severidad + ": " + nombreVar;
    }

    /**
     * Reconoce una alarma (cambia de ACTIVA a RECONOCIDA).
     */
    public Optional<AlarmaDTO> reconocerAlarma(Long alarmaId, String username) {
        Optional<Alarma> alarmaOpt = alarmaRepository.findById(alarmaId);
        if (alarmaOpt.isEmpty()) {
            return Optional.empty();
        }

        Alarma alarma = alarmaOpt.get();
        if (alarma.getEstado() != EstadoAlarma.ACTIVA) {
            LOG.warn("No se puede reconocer alarma {}: estado actual es {}", alarmaId, alarma.getEstado());
            return Optional.empty();
        }

        alarma.setEstado(EstadoAlarma.RECONOCIDA);
        if (username != null) {
            Optional<User> userOpt = userRepository.findOneByLogin(username);
            userOpt.ifPresent(alarma::setAcknowledgedBy);
        }

        Alarma saved = alarmaRepository.save(alarma);
        LOG.info("Alarma {} reconocida por {}", saved.getId(), username);

        AlarmaDTO dto = alarmaMapper.toDto(saved);
        alarmaWebSocketService.notificarAlarmaReconocida(dto);

        return Optional.of(dto);
    }

    /**
     * Finaliza una alarma manualmente (cambia a FINALIZADA).
     */
    public Optional<AlarmaDTO> finalizarAlarma(Long alarmaId) {
        Optional<Alarma> alarmaOpt = alarmaRepository.findById(alarmaId);
        if (alarmaOpt.isEmpty()) {
            return Optional.empty();
        }

        Alarma alarma = alarmaOpt.get();
        if (alarma.getEstado() == EstadoAlarma.FINALIZADA) {
            LOG.warn("Alarma {} ya está finalizada", alarmaId);
            return Optional.empty();
        }

        alarma.setEstado(EstadoAlarma.FINALIZADA);
        alarma.setDeactivatedAt(ZonedDateTime.now());

        Alarma saved = alarmaRepository.save(alarma);
        LOG.info("Alarma {} finalizada manualmente", saved.getId());

        AlarmaDTO dto = alarmaMapper.toDto(saved);
        alarmaWebSocketService.notificarAlarmaResuelta(dto);

        return Optional.of(dto);
    }

    /**
     * Obtiene alarmas no finalizadas (ACTIVA o RECONOCIDA) para un evento.
     */
    public List<AlarmaDTO> obtenerAlarmasActivasParaEvento(Long eventoId) {
        List<EstadoAlarma> estadosActivos = Arrays.asList(EstadoAlarma.ACTIVA, EstadoAlarma.RECONOCIDA);
        List<Alarma> alarmas = alarmaRepository.findByEventoIdAndEstadoIn(eventoId, estadosActivos);
        return alarmas.stream().map(alarmaMapper::toDto).toList();
    }

    public AlarmaDTO save(AlarmaDTO alarmaDTO) {
        LOG.debug("Request to save Alarma : {}", alarmaDTO);
        Alarma alarma = alarmaMapper.toEntity(alarmaDTO);
        alarma = alarmaRepository.save(alarma);
        return alarmaMapper.toDto(alarma);
    }

    public AlarmaDTO update(AlarmaDTO alarmaDTO) {
        LOG.debug("Request to update Alarma : {}", alarmaDTO);
        Alarma alarma = alarmaMapper.toEntity(alarmaDTO);
        alarma = alarmaRepository.save(alarma);
        return alarmaMapper.toDto(alarma);
    }

    public Optional<AlarmaDTO> partialUpdate(AlarmaDTO alarmaDTO) {
        LOG.debug("Request to partially update Alarma partially : {}", alarmaDTO);

        return alarmaRepository
            .findById(alarmaDTO.getId())
            .map(existingAlarma -> {
                alarmaMapper.partialUpdate(existingAlarma, alarmaDTO);
                return existingAlarma;
            })
            .map(alarmaRepository::save)
            .map(alarmaMapper::toDto);
    }

    public Page<AlarmaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return alarmaRepository.findAllWithEagerRelationships(pageable).map(alarmaMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<AlarmaDTO> findOne(Long id) {
        LOG.debug("Request to get Alarma : {}", id);
        return alarmaRepository.findOneWithEagerRelationships(id).map(alarmaMapper::toDto);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete Alarma : {}", id);
        alarmaRepository.deleteById(id);
    }
}
