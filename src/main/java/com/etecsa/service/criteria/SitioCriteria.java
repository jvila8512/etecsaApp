package com.etecsa.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.etecsa.domain.Sitio} entity. This class is used
 * in {@link com.etecsa.web.rest.SitioResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /sitios?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SitioCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter nombre;

    private StringFilter codigo;

    private StringFilter ubicacion;

    private ZonedDateTimeFilter fechaRegistro;

    private Boolean distinct;

    public SitioCriteria() {}

    public SitioCriteria(SitioCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.nombre = other.optionalNombre().map(StringFilter::copy).orElse(null);
        this.codigo = other.optionalCodigo().map(StringFilter::copy).orElse(null);
        this.ubicacion = other.optionalUbicacion().map(StringFilter::copy).orElse(null);
        this.fechaRegistro = other.optionalFechaRegistro().map(ZonedDateTimeFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public SitioCriteria copy() {
        return new SitioCriteria(this);
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

    public StringFilter getUbicacion() {
        return ubicacion;
    }

    public Optional<StringFilter> optionalUbicacion() {
        return Optional.ofNullable(ubicacion);
    }

    public StringFilter ubicacion() {
        if (ubicacion == null) {
            setUbicacion(new StringFilter());
        }
        return ubicacion;
    }

    public void setUbicacion(StringFilter ubicacion) {
        this.ubicacion = ubicacion;
    }

    public ZonedDateTimeFilter getFechaRegistro() {
        return fechaRegistro;
    }

    public Optional<ZonedDateTimeFilter> optionalFechaRegistro() {
        return Optional.ofNullable(fechaRegistro);
    }

    public ZonedDateTimeFilter fechaRegistro() {
        if (fechaRegistro == null) {
            setFechaRegistro(new ZonedDateTimeFilter());
        }
        return fechaRegistro;
    }

    public void setFechaRegistro(ZonedDateTimeFilter fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
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
        final SitioCriteria that = (SitioCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(nombre, that.nombre) &&
            Objects.equals(codigo, that.codigo) &&
            Objects.equals(ubicacion, that.ubicacion) &&
            Objects.equals(fechaRegistro, that.fechaRegistro) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, codigo, ubicacion, fechaRegistro, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SitioCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNombre().map(f -> "nombre=" + f + ", ").orElse("") +
            optionalCodigo().map(f -> "codigo=" + f + ", ").orElse("") +
            optionalUbicacion().map(f -> "ubicacion=" + f + ", ").orElse("") +
            optionalFechaRegistro().map(f -> "fechaRegistro=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
