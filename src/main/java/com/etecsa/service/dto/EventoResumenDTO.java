package com.etecsa.service.dto;

import java.io.Serializable;

/**
 * Resumen de una variable (EventoEquipo) para el dashboard.
 * Contiene solo los campos que el frontend necesita mostrar.
 */
public class EventoResumenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nombreVariable;
    private Double valorNumerico; // Null si es booleano
    private Boolean valorBooleano; // Null si es numérico
    private Integer dir; // dirección Modbus
    private String unidadMedida; // Para mostrar "50 Hz"
    private boolean esLectura; // Para diferenciar en el front
    private Double umbralAlerta; // Umbral para generar alarma
    private Boolean habilitarAlarma; // Habilitar deteccion de alarma

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreVariable() {
        return nombreVariable;
    }

    public void setNombreVariable(String nombreVariable) {
        this.nombreVariable = nombreVariable;
    }

    public Double getValorNumerico() {
        return valorNumerico;
    }

    public void setValorNumerico(Double valorNumerico) {
        this.valorNumerico = valorNumerico;
    }

    public Boolean getValorBooleano() {
        return valorBooleano;
    }

    public void setValorBooleano(Boolean valorBooleano) {
        this.valorBooleano = valorBooleano;
    }

    public Integer getDir() {
        return dir;
    }

    public void setDir(Integer dir) {
        this.dir = dir;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public boolean isEsLectura() {
        return esLectura;
    }

    public void setEsLectura(boolean esLectura) {
        this.esLectura = esLectura;
    }

    public Double getUmbralAlerta() {
        return umbralAlerta;
    }

    public void setUmbralAlerta(Double umbralAlerta) {
        this.umbralAlerta = umbralAlerta;
    }

    public Boolean getHabilitarAlarma() {
        return habilitarAlarma;
    }

    public void setHabilitarAlarma(Boolean habilitarAlarma) {
        this.habilitarAlarma = habilitarAlarma;
    }
}
