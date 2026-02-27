package com.etecsa.domain;

import com.etecsa.domain.enumeration.EstadoAlarma;
import com.etecsa.domain.enumeration.Severidad;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Registro de alertas y notificaciones.
 */
@Entity
@Table(name = "alarma")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Alarma implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @NotNull
    @Column(name = "activated_at", nullable = false)
    private ZonedDateTime activatedAt;

    @Column(name = "deactivated_at")
    private ZonedDateTime deactivatedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "severidad", nullable = false)
    private Severidad severidad;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoAlarma estado;

    @Column(name = "mensaje_usuario")
    private String mensajeUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "equipo", "plantilla" }, allowSetters = true)
    private EventoEquipo evento;

    @ManyToOne(fetch = FetchType.LAZY)
    private User acknowledgedBy;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Alarma id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public Alarma descripcion(String descripcion) {
        this.setDescripcion(descripcion);
        return this;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public ZonedDateTime getActivatedAt() {
        return this.activatedAt;
    }

    public Alarma activatedAt(ZonedDateTime activatedAt) {
        this.setActivatedAt(activatedAt);
        return this;
    }

    public void setActivatedAt(ZonedDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public ZonedDateTime getDeactivatedAt() {
        return this.deactivatedAt;
    }

    public Alarma deactivatedAt(ZonedDateTime deactivatedAt) {
        this.setDeactivatedAt(deactivatedAt);
        return this;
    }

    public void setDeactivatedAt(ZonedDateTime deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    public Severidad getSeveridad() {
        return this.severidad;
    }

    public Alarma severidad(Severidad severidad) {
        this.setSeveridad(severidad);
        return this;
    }

    public void setSeveridad(Severidad severidad) {
        this.severidad = severidad;
    }

    public EstadoAlarma getEstado() {
        return this.estado;
    }

    public Alarma estado(EstadoAlarma estado) {
        this.setEstado(estado);
        return this;
    }

    public void setEstado(EstadoAlarma estado) {
        this.estado = estado;
    }

    public String getMensajeUsuario() {
        return this.mensajeUsuario;
    }

    public Alarma mensajeUsuario(String mensajeUsuario) {
        this.setMensajeUsuario(mensajeUsuario);
        return this;
    }

    public void setMensajeUsuario(String mensajeUsuario) {
        this.mensajeUsuario = mensajeUsuario;
    }

    public EventoEquipo getEvento() {
        return this.evento;
    }

    public void setEvento(EventoEquipo eventoEquipo) {
        this.evento = eventoEquipo;
    }

    public Alarma evento(EventoEquipo eventoEquipo) {
        this.setEvento(eventoEquipo);
        return this;
    }

    public User getAcknowledgedBy() {
        return this.acknowledgedBy;
    }

    public void setAcknowledgedBy(User user) {
        this.acknowledgedBy = user;
    }

    public Alarma acknowledgedBy(User user) {
        this.setAcknowledgedBy(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Alarma)) {
            return false;
        }
        return getId() != null && getId().equals(((Alarma) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Alarma{" +
            "id=" + getId() +
            ", descripcion='" + getDescripcion() + "'" +
            ", activatedAt='" + getActivatedAt() + "'" +
            ", deactivatedAt='" + getDeactivatedAt() + "'" +
            ", severidad='" + getSeveridad() + "'" +
            ", estado='" + getEstado() + "'" +
            ", mensajeUsuario='" + getMensajeUsuario() + "'" +
            "}";
    }
}
