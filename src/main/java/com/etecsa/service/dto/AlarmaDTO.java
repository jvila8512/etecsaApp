package com.etecsa.service.dto;

import com.etecsa.domain.enumeration.EstadoAlarma;
import com.etecsa.domain.enumeration.Severidad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link com.etecsa.domain.Alarma} entity.
 */
@Schema(description = "Registro de alertas y notificaciones.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlarmaDTO implements Serializable {

    private Long id;

    @NotNull
    private String descripcion;

    @NotNull
    private ZonedDateTime activatedAt;

    private ZonedDateTime deactivatedAt;

    @NotNull
    private Severidad severidad;

    @NotNull
    private EstadoAlarma estado;

    private String mensajeUsuario;

    private EventoEquipoDTO evento;

    private UserDTO acknowledgedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public ZonedDateTime getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(ZonedDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public ZonedDateTime getDeactivatedAt() {
        return deactivatedAt;
    }

    public void setDeactivatedAt(ZonedDateTime deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    public Severidad getSeveridad() {
        return severidad;
    }

    public void setSeveridad(Severidad severidad) {
        this.severidad = severidad;
    }

    public EstadoAlarma getEstado() {
        return estado;
    }

    public void setEstado(EstadoAlarma estado) {
        this.estado = estado;
    }

    public String getMensajeUsuario() {
        return mensajeUsuario;
    }

    public void setMensajeUsuario(String mensajeUsuario) {
        this.mensajeUsuario = mensajeUsuario;
    }

    public EventoEquipoDTO getEvento() {
        return evento;
    }

    public void setEvento(EventoEquipoDTO evento) {
        this.evento = evento;
    }

    public UserDTO getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(UserDTO acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlarmaDTO)) {
            return false;
        }

        AlarmaDTO alarmaDTO = (AlarmaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, alarmaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlarmaDTO{" +
            "id=" + getId() +
            ", descripcion='" + getDescripcion() + "'" +
            ", activatedAt='" + getActivatedAt() + "'" +
            ", deactivatedAt='" + getDeactivatedAt() + "'" +
            ", severidad='" + getSeveridad() + "'" +
            ", estado='" + getEstado() + "'" +
            ", mensajeUsuario='" + getMensajeUsuario() + "'" +
            ", evento=" + getEvento() +
            ", acknowledgedBy=" + getAcknowledgedBy() +
            "}";
    }
}
