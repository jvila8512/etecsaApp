package com.etecsa.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para un equipo en el dashboard.
 * Incluye nombre, IP, estado y resumen de sus variables (eventos).
 */
public class DashboardEquipoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nombre;
    private String direccionIp;
    private String estado;
    private ZonedDateTime ultimoHeartbeat;
    private List<EventoResumenDTO> variables = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public ZonedDateTime getUltimoHeartbeat() {
        return ultimoHeartbeat;
    }

    public void setUltimoHeartbeat(ZonedDateTime ultimoHeartbeat) {
        this.ultimoHeartbeat = ultimoHeartbeat;
    }

    public List<EventoResumenDTO> getVariables() {
        return variables;
    }

    public void setVariables(List<EventoResumenDTO> variables) {
        this.variables = variables;
    }
}
