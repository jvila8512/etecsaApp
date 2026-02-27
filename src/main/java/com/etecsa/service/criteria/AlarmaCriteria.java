package com.etecsa.service.criteria;

import com.etecsa.domain.enumeration.EstadoAlarma;
import com.etecsa.domain.enumeration.Severidad;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.etecsa.domain.Alarma} entity. This class is used
 * in {@link com.etecsa.web.rest.AlarmaResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /alarmas?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlarmaCriteria implements Serializable, Criteria {

    /**
     * Class for filtering Severidad
     */
    public static class SeveridadFilter extends Filter<Severidad> {

        public SeveridadFilter() {}

        public SeveridadFilter(SeveridadFilter filter) {
            super(filter);
        }

        @Override
        public SeveridadFilter copy() {
            return new SeveridadFilter(this);
        }
    }

    /**
     * Class for filtering EstadoAlarma
     */
    public static class EstadoAlarmaFilter extends Filter<EstadoAlarma> {

        public EstadoAlarmaFilter() {}

        public EstadoAlarmaFilter(EstadoAlarmaFilter filter) {
            super(filter);
        }

        @Override
        public EstadoAlarmaFilter copy() {
            return new EstadoAlarmaFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter descripcion;

    private ZonedDateTimeFilter activatedAt;

    private ZonedDateTimeFilter deactivatedAt;

    private SeveridadFilter severidad;

    private EstadoAlarmaFilter estado;

    private StringFilter mensajeUsuario;

    private LongFilter eventoId;

    private LongFilter acknowledgedById;

    private Boolean distinct;

    public AlarmaCriteria() {}

    public AlarmaCriteria(AlarmaCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.descripcion = other.optionalDescripcion().map(StringFilter::copy).orElse(null);
        this.activatedAt = other.optionalActivatedAt().map(ZonedDateTimeFilter::copy).orElse(null);
        this.deactivatedAt = other.optionalDeactivatedAt().map(ZonedDateTimeFilter::copy).orElse(null);
        this.severidad = other.optionalSeveridad().map(SeveridadFilter::copy).orElse(null);
        this.estado = other.optionalEstado().map(EstadoAlarmaFilter::copy).orElse(null);
        this.mensajeUsuario = other.optionalMensajeUsuario().map(StringFilter::copy).orElse(null);
        this.eventoId = other.optionalEventoId().map(LongFilter::copy).orElse(null);
        this.acknowledgedById = other.optionalAcknowledgedById().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public AlarmaCriteria copy() {
        return new AlarmaCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getDescripcion() {
        return descripcion;
    }

    public Optional<StringFilter> optionalDescripcion() {
        return Optional.ofNullable(descripcion);
    }

    public StringFilter descripcion() {
        if (descripcion == null) {
            setDescripcion(new StringFilter());
        }
        return descripcion;
    }

    public void setDescripcion(StringFilter descripcion) {
        this.descripcion = descripcion;
    }

    public ZonedDateTimeFilter getActivatedAt() {
        return activatedAt;
    }

    public Optional<ZonedDateTimeFilter> optionalActivatedAt() {
        return Optional.ofNullable(activatedAt);
    }

    public ZonedDateTimeFilter activatedAt() {
        if (activatedAt == null) {
            setActivatedAt(new ZonedDateTimeFilter());
        }
        return activatedAt;
    }

    public void setActivatedAt(ZonedDateTimeFilter activatedAt) {
        this.activatedAt = activatedAt;
    }

    public ZonedDateTimeFilter getDeactivatedAt() {
        return deactivatedAt;
    }

    public Optional<ZonedDateTimeFilter> optionalDeactivatedAt() {
        return Optional.ofNullable(deactivatedAt);
    }

    public ZonedDateTimeFilter deactivatedAt() {
        if (deactivatedAt == null) {
            setDeactivatedAt(new ZonedDateTimeFilter());
        }
        return deactivatedAt;
    }

    public void setDeactivatedAt(ZonedDateTimeFilter deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    public SeveridadFilter getSeveridad() {
        return severidad;
    }

    public Optional<SeveridadFilter> optionalSeveridad() {
        return Optional.ofNullable(severidad);
    }

    public SeveridadFilter severidad() {
        if (severidad == null) {
            setSeveridad(new SeveridadFilter());
        }
        return severidad;
    }

    public void setSeveridad(SeveridadFilter severidad) {
        this.severidad = severidad;
    }

    public EstadoAlarmaFilter getEstado() {
        return estado;
    }

    public Optional<EstadoAlarmaFilter> optionalEstado() {
        return Optional.ofNullable(estado);
    }

    public EstadoAlarmaFilter estado() {
        if (estado == null) {
            setEstado(new EstadoAlarmaFilter());
        }
        return estado;
    }

    public void setEstado(EstadoAlarmaFilter estado) {
        this.estado = estado;
    }

    public StringFilter getMensajeUsuario() {
        return mensajeUsuario;
    }

    public Optional<StringFilter> optionalMensajeUsuario() {
        return Optional.ofNullable(mensajeUsuario);
    }

    public StringFilter mensajeUsuario() {
        if (mensajeUsuario == null) {
            setMensajeUsuario(new StringFilter());
        }
        return mensajeUsuario;
    }

    public void setMensajeUsuario(StringFilter mensajeUsuario) {
        this.mensajeUsuario = mensajeUsuario;
    }

    public LongFilter getEventoId() {
        return eventoId;
    }

    public Optional<LongFilter> optionalEventoId() {
        return Optional.ofNullable(eventoId);
    }

    public LongFilter eventoId() {
        if (eventoId == null) {
            setEventoId(new LongFilter());
        }
        return eventoId;
    }

    public void setEventoId(LongFilter eventoId) {
        this.eventoId = eventoId;
    }

    public LongFilter getAcknowledgedById() {
        return acknowledgedById;
    }

    public Optional<LongFilter> optionalAcknowledgedById() {
        return Optional.ofNullable(acknowledgedById);
    }

    public LongFilter acknowledgedById() {
        if (acknowledgedById == null) {
            setAcknowledgedById(new LongFilter());
        }
        return acknowledgedById;
    }

    public void setAcknowledgedById(LongFilter acknowledgedById) {
        this.acknowledgedById = acknowledgedById;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AlarmaCriteria that = (AlarmaCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(descripcion, that.descripcion) &&
            Objects.equals(activatedAt, that.activatedAt) &&
            Objects.equals(deactivatedAt, that.deactivatedAt) &&
            Objects.equals(severidad, that.severidad) &&
            Objects.equals(estado, that.estado) &&
            Objects.equals(mensajeUsuario, that.mensajeUsuario) &&
            Objects.equals(eventoId, that.eventoId) &&
            Objects.equals(acknowledgedById, that.acknowledgedById) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            descripcion,
            activatedAt,
            deactivatedAt,
            severidad,
            estado,
            mensajeUsuario,
            eventoId,
            acknowledgedById,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlarmaCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalDescripcion().map(f -> "descripcion=" + f + ", ").orElse("") +
            optionalActivatedAt().map(f -> "activatedAt=" + f + ", ").orElse("") +
            optionalDeactivatedAt().map(f -> "deactivatedAt=" + f + ", ").orElse("") +
            optionalSeveridad().map(f -> "severidad=" + f + ", ").orElse("") +
            optionalEstado().map(f -> "estado=" + f + ", ").orElse("") +
            optionalMensajeUsuario().map(f -> "mensajeUsuario=" + f + ", ").orElse("") +
            optionalEventoId().map(f -> "eventoId=" + f + ", ").orElse("") +
            optionalAcknowledgedById().map(f -> "acknowledgedById=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
