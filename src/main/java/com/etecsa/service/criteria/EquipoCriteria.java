package com.etecsa.service.criteria;

import com.etecsa.domain.enumeration.EstadoEquipo;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.etecsa.domain.Equipo} entity. This class is used
 * in {@link com.etecsa.web.rest.EquipoResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /equipos?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EquipoCriteria implements Serializable, Criteria {

    /**
     * Class for filtering EstadoEquipo
     */
    public static class EstadoEquipoFilter extends Filter<EstadoEquipo> {

        public EstadoEquipoFilter() {}

        public EstadoEquipoFilter(EstadoEquipoFilter filter) {
            super(filter);
        }

        @Override
        public EstadoEquipoFilter copy() {
            return new EstadoEquipoFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter nombre;

    private StringFilter direccionIp;

    private IntegerFilter modbusSlaveId;

    private StringFilter modelo;

    private StringFilter firmwareVersion;

    private EstadoEquipoFilter estado;

    private ZonedDateTimeFilter ultimoHeartbeat;

    private LongFilter sitioId;

    private LongFilter especialidadesId;

    private Boolean distinct;

    public EquipoCriteria() {}

    public EquipoCriteria(EquipoCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.nombre = other.optionalNombre().map(StringFilter::copy).orElse(null);
        this.direccionIp = other.optionalDireccionIp().map(StringFilter::copy).orElse(null);
        this.modbusSlaveId = other.optionalModbusSlaveId().map(IntegerFilter::copy).orElse(null);
        this.modelo = other.optionalModelo().map(StringFilter::copy).orElse(null);
        this.firmwareVersion = other.optionalFirmwareVersion().map(StringFilter::copy).orElse(null);
        this.estado = other.optionalEstado().map(EstadoEquipoFilter::copy).orElse(null);
        this.ultimoHeartbeat = other.optionalUltimoHeartbeat().map(ZonedDateTimeFilter::copy).orElse(null);
        this.sitioId = other.optionalSitioId().map(LongFilter::copy).orElse(null);
        this.especialidadesId = other.optionalEspecialidadesId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public EquipoCriteria copy() {
        return new EquipoCriteria(this);
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

    public StringFilter getDireccionIp() {
        return direccionIp;
    }

    public Optional<StringFilter> optionalDireccionIp() {
        return Optional.ofNullable(direccionIp);
    }

    public StringFilter direccionIp() {
        if (direccionIp == null) {
            setDireccionIp(new StringFilter());
        }
        return direccionIp;
    }

    public void setDireccionIp(StringFilter direccionIp) {
        this.direccionIp = direccionIp;
    }

    public IntegerFilter getModbusSlaveId() {
        return modbusSlaveId;
    }

    public Optional<IntegerFilter> optionalModbusSlaveId() {
        return Optional.ofNullable(modbusSlaveId);
    }

    public IntegerFilter modbusSlaveId() {
        if (modbusSlaveId == null) {
            setModbusSlaveId(new IntegerFilter());
        }
        return modbusSlaveId;
    }

    public void setModbusSlaveId(IntegerFilter modbusSlaveId) {
        this.modbusSlaveId = modbusSlaveId;
    }

    public StringFilter getModelo() {
        return modelo;
    }

    public Optional<StringFilter> optionalModelo() {
        return Optional.ofNullable(modelo);
    }

    public StringFilter modelo() {
        if (modelo == null) {
            setModelo(new StringFilter());
        }
        return modelo;
    }

    public void setModelo(StringFilter modelo) {
        this.modelo = modelo;
    }

    public StringFilter getFirmwareVersion() {
        return firmwareVersion;
    }

    public Optional<StringFilter> optionalFirmwareVersion() {
        return Optional.ofNullable(firmwareVersion);
    }

    public StringFilter firmwareVersion() {
        if (firmwareVersion == null) {
            setFirmwareVersion(new StringFilter());
        }
        return firmwareVersion;
    }

    public void setFirmwareVersion(StringFilter firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public EstadoEquipoFilter getEstado() {
        return estado;
    }

    public Optional<EstadoEquipoFilter> optionalEstado() {
        return Optional.ofNullable(estado);
    }

    public EstadoEquipoFilter estado() {
        if (estado == null) {
            setEstado(new EstadoEquipoFilter());
        }
        return estado;
    }

    public void setEstado(EstadoEquipoFilter estado) {
        this.estado = estado;
    }

    public ZonedDateTimeFilter getUltimoHeartbeat() {
        return ultimoHeartbeat;
    }

    public Optional<ZonedDateTimeFilter> optionalUltimoHeartbeat() {
        return Optional.ofNullable(ultimoHeartbeat);
    }

    public ZonedDateTimeFilter ultimoHeartbeat() {
        if (ultimoHeartbeat == null) {
            setUltimoHeartbeat(new ZonedDateTimeFilter());
        }
        return ultimoHeartbeat;
    }

    public void setUltimoHeartbeat(ZonedDateTimeFilter ultimoHeartbeat) {
        this.ultimoHeartbeat = ultimoHeartbeat;
    }

    public LongFilter getSitioId() {
        return sitioId;
    }

    public Optional<LongFilter> optionalSitioId() {
        return Optional.ofNullable(sitioId);
    }

    public LongFilter sitioId() {
        if (sitioId == null) {
            setSitioId(new LongFilter());
        }
        return sitioId;
    }

    public void setSitioId(LongFilter sitioId) {
        this.sitioId = sitioId;
    }

    public LongFilter getEspecialidadesId() {
        return especialidadesId;
    }

    public Optional<LongFilter> optionalEspecialidadesId() {
        return Optional.ofNullable(especialidadesId);
    }

    public LongFilter especialidadesId() {
        if (especialidadesId == null) {
            setEspecialidadesId(new LongFilter());
        }
        return especialidadesId;
    }

    public void setEspecialidadesId(LongFilter especialidadesId) {
        this.especialidadesId = especialidadesId;
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
        final EquipoCriteria that = (EquipoCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(nombre, that.nombre) &&
            Objects.equals(direccionIp, that.direccionIp) &&
            Objects.equals(modbusSlaveId, that.modbusSlaveId) &&
            Objects.equals(modelo, that.modelo) &&
            Objects.equals(firmwareVersion, that.firmwareVersion) &&
            Objects.equals(estado, that.estado) &&
            Objects.equals(ultimoHeartbeat, that.ultimoHeartbeat) &&
            Objects.equals(sitioId, that.sitioId) &&
            Objects.equals(especialidadesId, that.especialidadesId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            nombre,
            direccionIp,
            modbusSlaveId,
            modelo,
            firmwareVersion,
            estado,
            ultimoHeartbeat,
            sitioId,
            especialidadesId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EquipoCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNombre().map(f -> "nombre=" + f + ", ").orElse("") +
            optionalDireccionIp().map(f -> "direccionIp=" + f + ", ").orElse("") +
            optionalModbusSlaveId().map(f -> "modbusSlaveId=" + f + ", ").orElse("") +
            optionalModelo().map(f -> "modelo=" + f + ", ").orElse("") +
            optionalFirmwareVersion().map(f -> "firmwareVersion=" + f + ", ").orElse("") +
            optionalEstado().map(f -> "estado=" + f + ", ").orElse("") +
            optionalUltimoHeartbeat().map(f -> "ultimoHeartbeat=" + f + ", ").orElse("") +
            optionalSitioId().map(f -> "sitioId=" + f + ", ").orElse("") +
            optionalEspecialidadesId().map(f -> "especialidadesId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
