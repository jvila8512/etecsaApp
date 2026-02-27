package com.etecsa.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.etecsa.domain.EventoPlantilla} entity.
 */
@Schema(description = "Catálogo de variables comunes para reutilizar.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventoPlantillaDTO implements Serializable {

    private Long id;

    @NotNull
    private String nombre;

    private String descripcion;

    private Double scalingFactor;

    private String unidadMedida;

    private String funcionLectura;

    private String funcionEscritura;

    private EspecialidadDTO especialidad;

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getScalingFactor() {
        return scalingFactor;
    }

    public void setScalingFactor(Double scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getFuncionLectura() {
        return funcionLectura;
    }

    public void setFuncionLectura(String funcionLectura) {
        this.funcionLectura = funcionLectura;
    }

    public String getFuncionEscritura() {
        return funcionEscritura;
    }

    public void setFuncionEscritura(String funcionEscritura) {
        this.funcionEscritura = funcionEscritura;
    }

    public EspecialidadDTO getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(EspecialidadDTO especialidad) {
        this.especialidad = especialidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventoPlantillaDTO)) {
            return false;
        }

        EventoPlantillaDTO eventoPlantillaDTO = (EventoPlantillaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, eventoPlantillaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventoPlantillaDTO{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", scalingFactor=" + getScalingFactor() +
            ", unidadMedida='" + getUnidadMedida() + "'" +
            ", funcionLectura='" + getFuncionLectura() + "'" +
            ", funcionEscritura='" + getFuncionEscritura() + "'" +
            ", especialidad=" + getEspecialidad() +
            "}";
    }
}
