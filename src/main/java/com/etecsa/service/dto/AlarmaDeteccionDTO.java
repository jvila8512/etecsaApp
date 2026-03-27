package com.etecsa.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class AlarmaDeteccionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long eventoId;
    private String descripcion;
    private String severidad;
    private String mensajeUsuario;
    private Double valorActual;
    private Double umbral;
    private Boolean valorBooleano;
    private Boolean esAlarma;
    private ZonedDateTime timestamp;

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public String getMensajeUsuario() {
        return mensajeUsuario;
    }

    public void setMensajeUsuario(String mensajeUsuario) {
        this.mensajeUsuario = mensajeUsuario;
    }

    public Double getValorActual() {
        return valorActual;
    }

    public void setValorActual(Double valorActual) {
        this.valorActual = valorActual;
    }

    public Double getUmbral() {
        return umbral;
    }

    public void setUmbral(Double umbral) {
        this.umbral = umbral;
    }

    public Boolean getValorBooleano() {
        return valorBooleano;
    }

    public void setValorBooleano(Boolean valorBooleano) {
        this.valorBooleano = valorBooleano;
    }

    public Boolean getEsAlarma() {
        return esAlarma;
    }

    public void setEsAlarma(Boolean esAlarma) {
        this.esAlarma = esAlarma;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
