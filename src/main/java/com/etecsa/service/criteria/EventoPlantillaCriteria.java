package com.etecsa.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.etecsa.domain.EventoPlantilla} entity. This class is used
 * in {@link com.etecsa.web.rest.EventoPlantillaResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /evento-plantillas?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventoPlantillaCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter nombre;

    private StringFilter descripcion;

    private DoubleFilter scalingFactor;

    private StringFilter unidadMedida;

    private StringFilter funcionLectura;

    private StringFilter funcionEscritura;

    private LongFilter especialidadId;

    private Boolean distinct;

    public EventoPlantillaCriteria() {}

    public EventoPlantillaCriteria(EventoPlantillaCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.nombre = other.optionalNombre().map(StringFilter::copy).orElse(null);
        this.descripcion = other.optionalDescripcion().map(StringFilter::copy).orElse(null);
        this.scalingFactor = other.optionalScalingFactor().map(DoubleFilter::copy).orElse(null);
        this.unidadMedida = other.optionalUnidadMedida().map(StringFilter::copy).orElse(null);
        this.funcionLectura = other.optionalFuncionLectura().map(StringFilter::copy).orElse(null);
        this.funcionEscritura = other.optionalFuncionEscritura().map(StringFilter::copy).orElse(null);
        this.especialidadId = other.optionalEspecialidadId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public EventoPlantillaCriteria copy() {
        return new EventoPlantillaCriteria(this);
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

    public StringFilter getNombre() {
        return nombre;
    }

    public Optional<StringFilter> optionalNombre() {
        return Optional.ofNullable(nombre);
    }

    public StringFilter nombre() {
        if (nombre == null) {
            setNombre(new StringFilter());
        }
        return nombre;
    }

    public void setNombre(StringFilter nombre) {
        this.nombre = nombre;
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

    public DoubleFilter getScalingFactor() {
        return scalingFactor;
    }

    public Optional<DoubleFilter> optionalScalingFactor() {
        return Optional.ofNullable(scalingFactor);
    }

    public DoubleFilter scalingFactor() {
        if (scalingFactor == null) {
            setScalingFactor(new DoubleFilter());
        }
        return scalingFactor;
    }

    public void setScalingFactor(DoubleFilter scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    public StringFilter getUnidadMedida() {
        return unidadMedida;
    }

    public Optional<StringFilter> optionalUnidadMedida() {
        return Optional.ofNullable(unidadMedida);
    }

    public StringFilter unidadMedida() {
        if (unidadMedida == null) {
            setUnidadMedida(new StringFilter());
        }
        return unidadMedida;
    }

    public void setUnidadMedida(StringFilter unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public StringFilter getFuncionLectura() {
        return funcionLectura;
    }

    public Optional<StringFilter> optionalFuncionLectura() {
        return Optional.ofNullable(funcionLectura);
    }

    public StringFilter funcionLectura() {
        if (funcionLectura == null) {
            setFuncionLectura(new StringFilter());
        }
        return funcionLectura;
    }

    public void setFuncionLectura(StringFilter funcionLectura) {
        this.funcionLectura = funcionLectura;
    }

    public StringFilter getFuncionEscritura() {
        return funcionEscritura;
    }

    public Optional<StringFilter> optionalFuncionEscritura() {
        return Optional.ofNullable(funcionEscritura);
    }

    public StringFilter funcionEscritura() {
        if (funcionEscritura == null) {
            setFuncionEscritura(new StringFilter());
        }
        return funcionEscritura;
    }

    public void setFuncionEscritura(StringFilter funcionEscritura) {
        this.funcionEscritura = funcionEscritura;
    }

    public LongFilter getEspecialidadId() {
        return especialidadId;
    }

    public Optional<LongFilter> optionalEspecialidadId() {
        return Optional.ofNullable(especialidadId);
    }

    public LongFilter especialidadId() {
        if (especialidadId == null) {
            setEspecialidadId(new LongFilter());
        }
        return especialidadId;
    }

    public void setEspecialidadId(LongFilter especialidadId) {
        this.especialidadId = especialidadId;
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
        final EventoPlantillaCriteria that = (EventoPlantillaCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(nombre, that.nombre) &&
            Objects.equals(descripcion, that.descripcion) &&
            Objects.equals(scalingFactor, that.scalingFactor) &&
            Objects.equals(unidadMedida, that.unidadMedida) &&
            Objects.equals(funcionLectura, that.funcionLectura) &&
            Objects.equals(funcionEscritura, that.funcionEscritura) &&
            Objects.equals(especialidadId, that.especialidadId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            nombre,
            descripcion,
            scalingFactor,
            unidadMedida,
            funcionLectura,
            funcionEscritura,
            especialidadId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventoPlantillaCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNombre().map(f -> "nombre=" + f + ", ").orElse("") +
            optionalDescripcion().map(f -> "descripcion=" + f + ", ").orElse("") +
            optionalScalingFactor().map(f -> "scalingFactor=" + f + ", ").orElse("") +
            optionalUnidadMedida().map(f -> "unidadMedida=" + f + ", ").orElse("") +
            optionalFuncionLectura().map(f -> "funcionLectura=" + f + ", ").orElse("") +
            optionalFuncionEscritura().map(f -> "funcionEscritura=" + f + ", ").orElse("") +
            optionalEspecialidadId().map(f -> "especialidadId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
