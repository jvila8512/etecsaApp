package com.etecsa.service.dto;

import com.etecsa.domain.enumeration.Severidad;
import com.etecsa.domain.enumeration.TipoDato;
import com.etecsa.domain.enumeration.TipoRegistro;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link com.etecsa.domain.EventoEquipo} entity.
 */
@Schema(description = "Define el mapeo de memoria del PLC.\nConfigurable por el usuario para M (bits), MW (words), etc.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventoEquipoDTO implements Serializable {

    private Long id;

    @NotNull
    private String nombreVariable;

    @NotNull
    private Integer direccionModbus;

    @NotNull
    private TipoRegistro tipoRegistro;

    @NotNull
    private TipoDato tipoDato;

    private Boolean esEscribible;

    private Double valorNumerico;

    private Boolean valorBooleano;

    private ZonedDateTime timestampActualizacion;

    private Integer intervaloLectura;

    private Double umbralAlerta;

    private Boolean habilitarAlarma;

    private Severidad severidadAlerta;

    private EquipoDTO equipo;

    private EventoPlantillaDTO plantilla;

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

    public Integer getDireccionModbus() {
        return direccionModbus;
    }

    public void setDireccionModbus(Integer direccionModbus) {
        this.direccionModbus = direccionModbus;
    }

    public TipoRegistro getTipoRegistro() {
        return tipoRegistro;
    }

    public void setTipoRegistro(TipoRegistro tipoRegistro) {
        this.tipoRegistro = tipoRegistro;
    }

    public TipoDato getTipoDato() {
        return tipoDato;
    }

    public void setTipoDato(TipoDato tipoDato) {
        this.tipoDato = tipoDato;
    }

    public Boolean getEsEscribible() {
        return esEscribible;
    }

    public void setEsEscribible(Boolean esEscribible) {
        this.esEscribible = esEscribible;
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

    public ZonedDateTime getTimestampActualizacion() {
        return timestampActualizacion;
    }

    public void setTimestampActualizacion(ZonedDateTime timestampActualizacion) {
        this.timestampActualizacion = timestampActualizacion;
    }

    public Integer getIntervaloLectura() {
        return intervaloLectura;
    }

    public void setIntervaloLectura(Integer intervaloLectura) {
        this.intervaloLectura = intervaloLectura;
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

    public Severidad getSeveridadAlerta() {
        return severidadAlerta;
    }

    public void setSeveridadAlerta(Severidad severidadAlerta) {
        this.severidadAlerta = severidadAlerta;
    }

    public EquipoDTO getEquipo() {
        return equipo;
    }

    public void setEquipo(EquipoDTO equipo) {
        this.equipo = equipo;
    }

    public EventoPlantillaDTO getPlantilla() {
        return plantilla;
    }

    public void setPlantilla(EventoPlantillaDTO plantilla) {
        this.plantilla = plantilla;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventoEquipoDTO)) {
            return false;
        }

        EventoEquipoDTO eventoEquipoDTO = (EventoEquipoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, eventoEquipoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventoEquipoDTO{" +
            "id=" + getId() +
            ", nombreVariable='" + getNombreVariable() + "'" +
            ", direccionModbus=" + getDireccionModbus() +
            ", tipoRegistro='" + getTipoRegistro() + "'" +
            ", tipoDato='" + getTipoDato() + "'" +
            ", esEscribible='" + getEsEscribible() + "'" +
            ", valorNumerico=" + getValorNumerico() +
            ", valorBooleano='" + getValorBooleano() + "'" +
            ", timestampActualizacion='" + getTimestampActualizacion() + "'" +
            ", intervaloLectura=" + getIntervaloLectura() +
            ", umbralAlerta=" + getUmbralAlerta() +
            ", habilitarAlarma=" + getHabilitarAlarma() +
            ", equipo=" + getEquipo() +
            ", plantilla=" + getPlantilla() +
            "}";
    }
}
