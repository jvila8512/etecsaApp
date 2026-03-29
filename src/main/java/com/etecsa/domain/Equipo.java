package com.etecsa.domain;

import com.etecsa.domain.enumeration.EstadoEquipo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Representa el PLC o dispositivo físico.
 * Se añade modbusSlaveId para comunicación RTU/TCP.
 */
@Entity
@Table(name = "equipo")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Equipo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @NotNull
    @Column(name = "direccion_ip", nullable = false)
    private String direccionIp;

    @Column(name = "modbus_slave_id")
    private Integer modbusSlaveId;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEquipo estado;

    @Column(name = "ultimo_heartbeat")
    private ZonedDateTime ultimoHeartbeat;

    @Column(name = "intervalo_base")
    private Integer intervaloBase = 10;

    @Column(name = "critico")
    private Boolean critico = false;

    @ManyToOne(fetch = FetchType.LAZY)
    private Sitio sitio;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_equipo__especialidades",
        joinColumns = @JoinColumn(name = "equipo_id"),
        inverseJoinColumns = @JoinColumn(name = "especialidades_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "equipos" }, allowSetters = true)
    private Set<Especialidad> especialidades = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Equipo id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public Equipo nombre(String nombre) {
        this.setNombre(nombre);
        return this;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccionIp() {
        return this.direccionIp;
    }

    public Equipo direccionIp(String direccionIp) {
        this.setDireccionIp(direccionIp);
        return this;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public Integer getModbusSlaveId() {
        return this.modbusSlaveId;
    }

    public Equipo modbusSlaveId(Integer modbusSlaveId) {
        this.setModbusSlaveId(modbusSlaveId);
        return this;
    }

    public void setModbusSlaveId(Integer modbusSlaveId) {
        this.modbusSlaveId = modbusSlaveId;
    }

    public String getModelo() {
        return this.modelo;
    }

    public Equipo modelo(String modelo) {
        this.setModelo(modelo);
        return this;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getFirmwareVersion() {
        return this.firmwareVersion;
    }

    public Equipo firmwareVersion(String firmwareVersion) {
        this.setFirmwareVersion(firmwareVersion);
        return this;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public EstadoEquipo getEstado() {
        return this.estado;
    }

    public Equipo estado(EstadoEquipo estado) {
        this.setEstado(estado);
        return this;
    }

    public void setEstado(EstadoEquipo estado) {
        this.estado = estado;
    }

    public ZonedDateTime getUltimoHeartbeat() {
        return this.ultimoHeartbeat;
    }

    public Equipo ultimoHeartbeat(ZonedDateTime ultimoHeartbeat) {
        this.setUltimoHeartbeat(ultimoHeartbeat);
        return this;
    }

    public void setUltimoHeartbeat(ZonedDateTime ultimoHeartbeat) {
        this.ultimoHeartbeat = ultimoHeartbeat;
    }

    public Integer getIntervaloBase() {
        return this.intervaloBase;
    }

    public Equipo intervaloBase(Integer intervaloBase) {
        this.setIntervaloBase(intervaloBase);
        return this;
    }

    public void setIntervaloBase(Integer intervaloBase) {
        this.intervaloBase = intervaloBase;
    }

    public Boolean getCritico() {
        return this.critico;
    }

    public Equipo critico(Boolean critico) {
        this.setCritico(critico);
        return this;
    }

    public void setCritico(Boolean critico) {
        this.critico = critico;
    }

    public Sitio getSitio() {
        return this.sitio;
    }

    public void setSitio(Sitio sitio) {
        this.sitio = sitio;
    }

    public Equipo sitio(Sitio sitio) {
        this.setSitio(sitio);
        return this;
    }

    public Set<Especialidad> getEspecialidades() {
        return this.especialidades;
    }

    public void setEspecialidades(Set<Especialidad> especialidads) {
        this.especialidades = especialidads;
    }

    public Equipo especialidades(Set<Especialidad> especialidads) {
        this.setEspecialidades(especialidads);
        return this;
    }

    public Equipo addEspecialidades(Especialidad especialidad) {
        this.especialidades.add(especialidad);
        return this;
    }

    public Equipo removeEspecialidades(Especialidad especialidad) {
        this.especialidades.remove(especialidad);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Equipo)) {
            return false;
        }
        return getId() != null && getId().equals(((Equipo) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Equipo{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", direccionIp='" + getDireccionIp() + "'" +
            ", modbusSlaveId=" + getModbusSlaveId() +
            ", modelo='" + getModelo() + "'" +
            ", firmwareVersion='" + getFirmwareVersion() + "'" +
            ", estado='" + getEstado() + "'" +
            ", ultimoHeartbeat='" + getUltimoHeartbeat() + "'" +
            "}";
    }
}
