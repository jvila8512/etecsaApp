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
 * Servicio que envía cada 5 segundos el estado de todos los equipos
 * y sus variables al tópico /topic/dashboard.
 * Solo los usuarios autenticados reciben estos datos (configurado en WebsocketSecurityConfiguration).
 */
@Service
public class DashboardWebSocketService {

    private static final Logger log = LoggerFactory.getLogger(DashboardWebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final EquipoRepository equipoRepository;
    private final PollingService pollingService; // <-- Solo necesitamos este servicio

    public DashboardWebSocketService(
        SimpMessagingTemplate messagingTemplate,
        EquipoRepository equipoRepository,
        PollingService pollingService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.equipoRepository = equipoRepository;
        this.pollingService = pollingService;
    }

    @Scheduled(fixedRate = 5000) // Se ejecuta cada 5 segundos
    public void ejecutarCicloDeLectura() {
        try {
            // Pega esto en cualquier lugar que se ejecute una vez, por ejemplo en el conectar
            // Busca en los logs el valor 1800 = 0x0708

            log.info("Iniciando ciclo de lectura para 200 equipos...");
            List<Equipo> equipos = equipoRepository.findAll();

            // 1. LANZAMOS TODAS LAS LECTURAS EN PARALELO
            // pollingService.procesarEquipo debe encargarse de Modbus y BD
            List<CompletableFuture<DashboardEquipoDTO>> futures = equipos
                .stream()
                .map(pollingService::procesarEquipo)
                .collect(Collectors.toList());

            // 2. ESPERAMOS A QUE TODOS LOS HILOS TERMINEN
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(v -> {
                // 3. RECOLECTAMOS RESULTADOS
                List<DashboardEquipoDTO> resultados = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());

                // 4. CONSTRUIMOS EL DTO FINAL
                DashboardDTO dto = new DashboardDTO();
                dto.setTimestamp(System.currentTimeMillis());
                dto.setEquipos(resultados);

                // 5. ENVIAMOS POR WEBSOCKET (Estructura única)
                messagingTemplate.convertAndSend("/topic/dashboard", dto);
                log.info("Dashboard real-time enviado con {} equipos", resultados.size());
            });
        } catch (Exception e) {
            log.error("Error crítico en ciclo de lectura asíncrono: {}", e.getMessage());
        }
    }
}
