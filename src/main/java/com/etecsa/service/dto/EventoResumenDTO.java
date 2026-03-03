package com.etecsa.service.dto;

import java.io.Serializable;

/**
 * Resumen de una variable (EventoEquipo) para el dashboard.
 * Contiene solo los campos que el frontend necesita mostrar.
 */
public class EventoResumenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombreVariable;
    private Double valorNumerico; // Null si es booleano
    private Boolean valorBooleano; // Null si es numérico
    private String unidadMedida; // Para mostrar "50 Hz"
    private boolean esLectura; // Para diferenciar en el front

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
}
