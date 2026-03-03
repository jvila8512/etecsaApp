package com.etecsa.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO principal para el dashboard en tiempo real.
 * Contiene la lista de equipos con su estado y variables.
 * Se envía cada 5 segundos por WebSocket al tópico /topic/dashboard.
 */
public class DashboardDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long timestamp;
    private List<DashboardEquipoDTO> equipos = new ArrayList<>();

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<DashboardEquipoDTO> getEquipos() {
        return equipos;
    }

    public void setEquipos(List<DashboardEquipoDTO> equipos) {
        this.equipos = equipos;
    }
}
