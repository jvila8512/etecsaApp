package com.etecsa.service.dashboard;

import com.etecsa.domain.Equipo;
import com.etecsa.repository.EquipoRepository;
import com.etecsa.service.dto.DashboardDTO;
import com.etecsa.service.dto.DashboardEquipoDTO;
import com.etecsa.service.modbus.PollingService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Servicio que envía el estado de los equipos al tópico /topic/dashboard.
 *
 * Dos tipos de envío:
 * 1. Dashboard completo: envía TODOS los equipos con TODAS las variables (para la vista)
 * 2. Actualizaciones rápidas: según intervalos, para detección de alarmas
 *
 * Solo los usuarios autenticados reciben estos datos.
 */
@Service
public class DashboardWebSocketService {

    private static final Logger log = LoggerFactory.getLogger(DashboardWebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final EquipoRepository equipoRepository;
    private final PollingService pollingService;

    public DashboardWebSocketService(
        SimpMessagingTemplate messagingTemplate,
        EquipoRepository equipoRepository,
        PollingService pollingService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.equipoRepository = equipoRepository;
        this.pollingService = pollingService;
    }

    /**
     * Envía el dashboard completo con TODOS los equipos y variables.
     * Se ejecuta cada 5 segundos para la vista del dashboard.
     */
    @Scheduled(fixedRate = 5000) // Cada 5 segundos
    public void enviarDashboardCompleto() {
        try {
            List<Equipo> equipos = equipoRepository.findAll();

            List<CompletableFuture<DashboardEquipoDTO>> futures = equipos
                .stream()
                .map(eq -> pollingService.procesarEquipo(eq, false, false))
                .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(v -> {
                List<DashboardEquipoDTO> resultados = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());

                DashboardDTO dto = new DashboardDTO();
                dto.setTimestamp(System.currentTimeMillis());
                dto.setEquipos(resultados);

                messagingTemplate.convertAndSend("/topic/dashboard/completo", dto);
                log.debug("Dashboard completo enviado con {} equipos", resultados.size());
            });
        } catch (Exception e) {
            log.error("Error enviando dashboard completo: {}", e.getMessage());
        }
    }

    /**
     * Envía actualizaciones rápidas según intervalos de cada variable.
     * Se ejecuta cada 500ms para detección instantánea de alarmas críticas.
     *
     * Intervalos dinámicos:
     * - Booleanos con umbral: cada 1 tick (500ms) = INSTANTÁNEO
     * - Numéricos con umbral: cada 4 ticks (2s)
     * - Equipos críticos: cada 6 ticks (3s)
     * - Otros: según configurado
     */
    @Scheduled(fixedRate = 500) // Cada 500ms
    public void enviarActualizacionesRapidas() {
        log.error("######################### INICIO ACTUALIZACIONES RAPIDAS #########################");
        try {
            List<Equipo> equipos = equipoRepository.findAll();

            List<CompletableFuture<DashboardEquipoDTO>> futures = equipos
                .stream()
                .map(eq -> pollingService.procesarEquipo(eq, true, true)) // true, true = filtrar por intervalo, detectar alarmas
                .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(v -> {
                List<DashboardEquipoDTO> resultados = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());

                if (!resultados.isEmpty()) {
                    DashboardDTO dto = new DashboardDTO();
                    dto.setTimestamp(System.currentTimeMillis());
                    dto.setEquipos(resultados);

                    messagingTemplate.convertAndSend("/topic/dashboard/actualizaciones", dto);
                    log.trace("Actualizaciones rápidas enviadas con {} equipos", resultados.size());
                }
            });
        } catch (Exception e) {
            log.error("Error en actualizaciones rápidas: {}", e.getMessage());
        }
    }
}
