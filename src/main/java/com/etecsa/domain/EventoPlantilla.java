package com.etecsa.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Catálogo de variables comunes para reutilizar.
 */
@Entity
@Table(name = "evento_plantilla")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventoPlantilla implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "scaling_factor")
    private Double scalingFactor;

    @Column(name = "unidad_medida")
    private String unidadMedida;

    @Column(name = "funcion_lectura")
    private String funcionLectura;

    @Column(name = "funcion_escritura")
    private String funcionEscritura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "equipos" }, allowSetters = true)
    private Especialidad especialidad;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public EventoPlantilla id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public EventoPlantilla nombre(String nombre) {
        this.setNombre(nombre);
        return this;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public EventoPlantilla descripcion(String descripcion) {
        this.setDescripcion(descripcion);
        return this;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getScalingFactor() {
        return this.scalingFactor;
    }

    public EventoPlantilla scalingFactor(Double scalingFactor) {
        this.setScalingFactor(scalingFactor);
        return this;
    }

    public void setScalingFactor(Double scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    public String getUnidadMedida() {
        return this.unidadMedida;
    }

    public EventoPlantilla unidadMedida(String unidadMedida) {
        this.setUnidadMedida(unidadMedida);
        return this;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getFuncionLectura() {
        return this.funcionLectura;
    }

    public EventoPlantilla funcionLectura(String funcionLectura) {
        this.setFuncionLectura(funcionLectura);
        return this;
    }

    public void setFuncionLectura(String funcionLectura) {
        this.funcionLectura = funcionLectura;
    }

    public String getFuncionEscritura() {
        return this.funcionEscritura;
    }

    public EventoPlantilla funcionEscritura(String funcionEscritura) {
        this.setFuncionEscritura(funcionEscritura);
        return this;
    }

    public void setFuncionEscritura(String funcionEscritura) {
        this.funcionEscritura = funcionEscritura;
    }

    public Especialidad getEspecialidad() {
        return this.especialidad;
    }

    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad;
    }

    public EventoPlantilla especialidad(Especialidad especialidad) {
        this.setEspecialidad(especialidad);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventoPlantilla)) {
            return false;
        }
        return getId() != null && getId().equals(((EventoPlantilla) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventoPlantilla{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", scalingFactor=" + getScalingFactor() +
            ", unidadMedida='" + getUnidadMedida() + "'" +
            ", funcionLectura='" + getFuncionLectura() + "'" +
            ", funcionEscritura='" + getFuncionEscritura() + "'" +
            "}";
    }
}
