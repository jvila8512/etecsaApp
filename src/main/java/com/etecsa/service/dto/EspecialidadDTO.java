package com.etecsa.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.etecsa.domain.Especialidad} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EspecialidadDTO implements Serializable {

    private Long id;

    @NotNull
    private String nombre;

    @NotNull
    private String codigo;

    private String descripcionTecnica;

    private Set<EquipoDTO> equipos = new HashSet<>();

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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcionTecnica() {
        return descripcionTecnica;
    }

    public void setDescripcionTecnica(String descripcionTecnica) {
        this.descripcionTecnica = descripcionTecnica;
    }

    public Set<EquipoDTO> getEquipos() {
        return equipos;
    }

    public void setEquipos(Set<EquipoDTO> equipos) {
        this.equipos = equipos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EspecialidadDTO)) {
            return false;
        }

        EspecialidadDTO especialidadDTO = (EspecialidadDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, especialidadDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EspecialidadDTO{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", codigo='" + getCodigo() + "'" +
            ", descripcionTecnica='" + getDescripcionTecnica() + "'" +
            ", equipos=" + getEquipos() +
            "}";
    }
}
