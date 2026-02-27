package com.etecsa.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.etecsa.domain.Especialidad} entity. This class is used
 * in {@link com.etecsa.web.rest.EspecialidadResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /especialidads?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EspecialidadCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter nombre;

    private StringFilter codigo;

    private StringFilter descripcionTecnica;

    private LongFilter equiposId;

    private Boolean distinct;

    public EspecialidadCriteria() {}

    public EspecialidadCriteria(EspecialidadCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.nombre = other.optionalNombre().map(StringFilter::copy).orElse(null);
        this.codigo = other.optionalCodigo().map(StringFilter::copy).orElse(null);
        this.descripcionTecnica = other.optionalDescripcionTecnica().map(StringFilter::copy).orElse(null);
        this.equiposId = other.optionalEquiposId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public EspecialidadCriteria copy() {
        return new EspecialidadCriteria(this);
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

    public StringFilter getCodigo() {
        return codigo;
    }

    public Optional<StringFilter> optionalCodigo() {
        return Optional.ofNullable(codigo);
    }

    public StringFilter codigo() {
        if (codigo == null) {
            setCodigo(new StringFilter());
        }
        return codigo;
    }

    public void setCodigo(StringFilter codigo) {
        this.codigo = codigo;
    }

    public StringFilter getDescripcionTecnica() {
        return descripcionTecnica;
    }

    public Optional<StringFilter> optionalDescripcionTecnica() {
        return Optional.ofNullable(descripcionTecnica);
    }

    public StringFilter descripcionTecnica() {
        if (descripcionTecnica == null) {
            setDescripcionTecnica(new StringFilter());
        }
        return descripcionTecnica;
    }

    public void setDescripcionTecnica(StringFilter descripcionTecnica) {
        this.descripcionTecnica = descripcionTecnica;
    }

    public LongFilter getEquiposId() {
        return equiposId;
    }

    public Optional<LongFilter> optionalEquiposId() {
        return Optional.ofNullable(equiposId);
    }

    public LongFilter equiposId() {
        if (equiposId == null) {
            setEquiposId(new LongFilter());
        }
        return equiposId;
    }

    public void setEquiposId(LongFilter equiposId) {
        this.equiposId = equiposId;
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
        final EspecialidadCriteria that = (EspecialidadCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(nombre, that.nombre) &&
            Objects.equals(codigo, that.codigo) &&
            Objects.equals(descripcionTecnica, that.descripcionTecnica) &&
            Objects.equals(equiposId, that.equiposId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, codigo, descripcionTecnica, equiposId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EspecialidadCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNombre().map(f -> "nombre=" + f + ", ").orElse("") +
            optionalCodigo().map(f -> "codigo=" + f + ", ").orElse("") +
            optionalDescripcionTecnica().map(f -> "descripcionTecnica=" + f + ", ").orElse("") +
            optionalEquiposId().map(f -> "equiposId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
