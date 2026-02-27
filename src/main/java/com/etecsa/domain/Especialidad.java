package com.etecsa.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Especialidad.
 */
@Entity
@Table(name = "especialidad")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Especialidad implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @NotNull
    @Column(name = "codigo", nullable = false, unique = true)
    private String codigo;

    @Column(name = "descripcion_tecnica")
    private String descripcionTecnica;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "especialidades")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "sitio", "especialidades" }, allowSetters = true)
    private Set<Equipo> equipos = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Especialidad id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public Especialidad nombre(String nombre) {
        this.setNombre(nombre);
        return this;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return this.codigo;
    }

    public Especialidad codigo(String codigo) {
        this.setCodigo(codigo);
        return this;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcionTecnica() {
        return this.descripcionTecnica;
    }

    public Especialidad descripcionTecnica(String descripcionTecnica) {
        this.setDescripcionTecnica(descripcionTecnica);
        return this;
    }

    public void setDescripcionTecnica(String descripcionTecnica) {
        this.descripcionTecnica = descripcionTecnica;
    }

    public Set<Equipo> getEquipos() {
        return this.equipos;
    }

    public void setEquipos(Set<Equipo> equipos) {
        if (this.equipos != null) {
            this.equipos.forEach(i -> i.removeEspecialidades(this));
        }
        if (equipos != null) {
            equipos.forEach(i -> i.addEspecialidades(this));
        }
        this.equipos = equipos;
    }

    public Especialidad equipos(Set<Equipo> equipos) {
        this.setEquipos(equipos);
        return this;
    }

    public Especialidad addEquipos(Equipo equipo) {
        this.equipos.add(equipo);
        equipo.getEspecialidades().add(this);
        return this;
    }

    public Especialidad removeEquipos(Equipo equipo) {
        this.equipos.remove(equipo);
        equipo.getEspecialidades().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Especialidad)) {
            return false;
        }
        return getId() != null && getId().equals(((Especialidad) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Especialidad{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", codigo='" + getCodigo() + "'" +
            ", descripcionTecnica='" + getDescripcionTecnica() + "'" +
            "}";
    }
}
