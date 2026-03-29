package com.etecsa.domain;

import com.etecsa.domain.enumeration.Severidad;
import com.etecsa.domain.enumeration.TipoDato;
import com.etecsa.domain.enumeration.TipoRegistro;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Define el mapeo de memoria del PLC.
 * Configurable por el usuario para M (bits), MW (words), etc.
 */
@Entity
@Table(name = "evento_equipo")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventoEquipo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "nombre_variable", nullable = false)
    private String nombreVariable;

    @NotNull
    @Column(name = "direccion_modbus", nullable = false)
    private Integer direccionModbus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_registro", nullable = false)
    private TipoRegistro tipoRegistro;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dato", nullable = false)
    private TipoDato tipoDato;

    @Column(name = "es_escribible")
    private Boolean esEscribible;

    @Column(name = "valor_numerico")
    private Double valorNumerico;

    @Column(name = "valor_booleano")
    private Boolean valorBooleano;

    @Column(name = "timestamp_actualizacion")
    private ZonedDateTime timestampActualizacion;

    @Column(name = "intervalo_lectura")
    private Integer intervaloLectura;

    @Column(name = "umbral_alerta")
    private Double umbralAlerta;

    @Enumerated(EnumType.STRING)
    @Column(name = "severidad_alerta")
    private Severidad severidadAlerta;

    @Column(name = "habilitar_alarma")
    private Boolean habilitarAlarma = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "sitio", "especialidades" }, allowSetters = true)
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "especialidad" }, allowSetters = true)
    private EventoPlantilla plantilla;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public EventoEquipo id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreVariable() {
        return this.nombreVariable;
    }

    public EventoEquipo nombreVariable(String nombreVariable) {
        this.setNombreVariable(nombreVariable);
        return this;
    }

    public void setNombreVariable(String nombreVariable) {
        this.nombreVariable = nombreVariable;
    }

    public Integer getDireccionModbus() {
        return this.direccionModbus;
    }

    public EventoEquipo direccionModbus(Integer direccionModbus) {
        this.setDireccionModbus(direccionModbus);
        return this;
    }

    public void setDireccionModbus(Integer direccionModbus) {
        this.direccionModbus = direccionModbus;
    }

    public TipoRegistro getTipoRegistro() {
        return this.tipoRegistro;
    }

    public EventoEquipo tipoRegistro(TipoRegistro tipoRegistro) {
        this.setTipoRegistro(tipoRegistro);
        return this;
    }

    public void setTipoRegistro(TipoRegistro tipoRegistro) {
        this.tipoRegistro = tipoRegistro;
    }

    public TipoDato getTipoDato() {
        return this.tipoDato;
    }

    public EventoEquipo tipoDato(TipoDato tipoDato) {
        this.setTipoDato(tipoDato);
        return this;
    }

    public void setTipoDato(TipoDato tipoDato) {
        this.tipoDato = tipoDato;
    }

    public Boolean getEsEscribible() {
        return this.esEscribible;
    }

    public EventoEquipo esEscribible(Boolean esEscribible) {
        this.setEsEscribible(esEscribible);
        return this;
    }

    public void setEsEscribible(Boolean esEscribible) {
        this.esEscribible = esEscribible;
    }

    public Double getValorNumerico() {
        return this.valorNumerico;
    }

    public EventoEquipo valorNumerico(Double valorNumerico) {
        this.setValorNumerico(valorNumerico);
        return this;
    }

    public void setValorNumerico(Double valorNumerico) {
        this.valorNumerico = valorNumerico;
    }

    public Boolean getValorBooleano() {
        return this.valorBooleano;
    }

    public EventoEquipo valorBooleano(Boolean valorBooleano) {
        this.setValorBooleano(valorBooleano);
        return this;
    }

    public void setValorBooleano(Boolean valorBooleano) {
        this.valorBooleano = valorBooleano;
    }

    public ZonedDateTime getTimestampActualizacion() {
        return this.timestampActualizacion;
    }

    public EventoEquipo timestampActualizacion(ZonedDateTime timestampActualizacion) {
        this.setTimestampActualizacion(timestampActualizacion);
        return this;
    }

    public void setTimestampActualizacion(ZonedDateTime timestampActualizacion) {
        this.timestampActualizacion = timestampActualizacion;
    }

    public Integer getIntervaloLectura() {
        return this.intervaloLectura;
    }

    public EventoEquipo intervaloLectura(Integer intervaloLectura) {
        this.setIntervaloLectura(intervaloLectura);
        return this;
    }

    public void setIntervaloLectura(Integer intervaloLectura) {
        this.intervaloLectura = intervaloLectura;
    }

    public Double getUmbralAlerta() {
        return this.umbralAlerta;
    }

    public EventoEquipo umbralAlerta(Double umbralAlerta) {
        this.setUmbralAlerta(umbralAlerta);
        return this;
    }

    public void setUmbralAlerta(Double umbralAlerta) {
        this.umbralAlerta = umbralAlerta;
    }

    public Severidad getSeveridadAlerta() {
        return this.severidadAlerta;
    }

    public EventoEquipo severidadAlerta(Severidad severidadAlerta) {
        this.setSeveridadAlerta(severidadAlerta);
        return this;
    }

    public void setSeveridadAlerta(Severidad severidadAlerta) {
        this.severidadAlerta = severidadAlerta;
    }

    public Boolean getHabilitarAlarma() {
        return this.habilitarAlarma;
    }

    public EventoEquipo habilitarAlarma(Boolean habilitarAlarma) {
        this.setHabilitarAlarma(habilitarAlarma);
        return this;
    }

    public void setHabilitarAlarma(Boolean habilitarAlarma) {
        this.habilitarAlarma = habilitarAlarma;
    }

    public Equipo getEquipo() {
        return this.equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public EventoEquipo equipo(Equipo equipo) {
        this.setEquipo(equipo);
        return this;
    }

    public EventoPlantilla getPlantilla() {
        return this.plantilla;
    }

    public void setPlantilla(EventoPlantilla eventoPlantilla) {
        this.plantilla = eventoPlantilla;
    }

    public EventoEquipo plantilla(EventoPlantilla eventoPlantilla) {
        this.setPlantilla(eventoPlantilla);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventoEquipo)) {
            return false;
        }
        return getId() != null && getId().equals(((EventoEquipo) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventoEquipo{" +
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
            "}";
    }
}
