package com.etecsa.service.dto;

import com.etecsa.domain.enumeration.EstadoEquipo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.etecsa.domain.Equipo} entity.
 */
@Schema(description = "Representa el PLC o dispositivo físico.\nSe añade modbusSlaveId para comunicación RTU/TCP.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EquipoDTO implements Serializable {

    private Long id;

    @NotNull
    private String nombre;

    @NotNull
    private String direccionIp;

    private Integer modbusSlaveId;

    private String modelo;

    private String firmwareVersion;

    @NotNull
    private EstadoEquipo estado;

    private ZonedDateTime ultimoHeartbeat;

    private SitioDTO sitio;

    private Set<EspecialidadDTO> especialidades = new HashSet<>();

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

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public Integer getModbusSlaveId() {
        return modbusSlaveId;
    }

    public void setModbusSlaveId(Integer modbusSlaveId) {
        this.modbusSlaveId = modbusSlaveId;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public EstadoEquipo getEstado() {
        return estado;
    }

    public void setEstado(EstadoEquipo estado) {
        this.estado = estado;
    }

    public ZonedDateTime getUltimoHeartbeat() {
        return ultimoHeartbeat;
    }

    public void setUltimoHeartbeat(ZonedDateTime ultimoHeartbeat) {
        this.ultimoHeartbeat = ultimoHeartbeat;
    }

    public SitioDTO getSitio() {
        return sitio;
    }

    public void setSitio(SitioDTO sitio) {
        this.sitio = sitio;
    }

    public Set<EspecialidadDTO> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(Set<EspecialidadDTO> especialidades) {
        this.especialidades = especialidades;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EquipoDTO)) {
            return false;
        }

        EquipoDTO equipoDTO = (EquipoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, equipoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EquipoDTO{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", direccionIp='" + getDireccionIp() + "'" +
            ", modbusSlaveId=" + getModbusSlaveId() +
            ", modelo='" + getModelo() + "'" +
            ", firmwareVersion='" + getFirmwareVersion() + "'" +
            ", estado='" + getEstado() + "'" +
            ", ultimoHeartbeat='" + getUltimoHeartbeat() + "'" +
            ", sitio=" + getSitio() +
            ", especialidades=" + getEspecialidades() +
            "}";
    }
}
