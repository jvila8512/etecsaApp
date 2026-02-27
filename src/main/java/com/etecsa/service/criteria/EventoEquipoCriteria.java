package com.etecsa.service.criteria;

import com.etecsa.domain.enumeration.TipoDato;
import com.etecsa.domain.enumeration.TipoRegistro;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.etecsa.domain.EventoEquipo} entity. This class is used
 * in {@link com.etecsa.web.rest.EventoEquipoResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /evento-equipos?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EventoEquipoCriteria implements Serializable, Criteria {

    /**
     * Class for filtering TipoRegistro
     */
    public static class TipoRegistroFilter extends Filter<TipoRegistro> {

        public TipoRegistroFilter() {}

        public TipoRegistroFilter(TipoRegistroFilter filter) {
            super(filter);
        }

        @Override
        public TipoRegistroFilter copy() {
            return new TipoRegistroFilter(this);
        }
    }

    /**
     * Class for filtering TipoDato
     */
    public static class TipoDatoFilter extends Filter<TipoDato> {

        public TipoDatoFilter() {}

        public TipoDatoFilter(TipoDatoFilter filter) {
            super(filter);
        }

        @Override
        public TipoDatoFilter copy() {
            return new TipoDatoFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter nombreVariable;

    private IntegerFilter direccionModbus;

    private TipoRegistroFilter tipoRegistro;

    private TipoDatoFilter tipoDato;

    private BooleanFilter esEscribible;

    private DoubleFilter valorNumerico;

    private BooleanFilter valorBooleano;

    private ZonedDateTimeFilter timestampActualizacion;

    private IntegerFilter intervaloLectura;

    private DoubleFilter umbralAlerta;

    private LongFilter equipoId;

    private LongFilter plantillaId;

    private Boolean distinct;

    public EventoEquipoCriteria() {}

    public EventoEquipoCriteria(EventoEquipoCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.nombreVariable = other.optionalNombreVariable().map(StringFilter::copy).orElse(null);
        this.direccionModbus = other.optionalDireccionModbus().map(IntegerFilter::copy).orElse(null);
        this.tipoRegistro = other.optionalTipoRegistro().map(TipoRegistroFilter::copy).orElse(null);
        this.tipoDato = other.optionalTipoDato().map(TipoDatoFilter::copy).orElse(null);
        this.esEscribible = other.optionalEsEscribible().map(BooleanFilter::copy).orElse(null);
        this.valorNumerico = other.optionalValorNumerico().map(DoubleFilter::copy).orElse(null);
        this.valorBooleano = other.optionalValorBooleano().map(BooleanFilter::copy).orElse(null);
        this.timestampActualizacion = other.optionalTimestampActualizacion().map(ZonedDateTimeFilter::copy).orElse(null);
        this.intervaloLectura = other.optionalIntervaloLectura().map(IntegerFilter::copy).orElse(null);
        this.umbralAlerta = other.optionalUmbralAlerta().map(DoubleFilter::copy).orElse(null);
        this.equipoId = other.optionalEquipoId().map(LongFilter::copy).orElse(null);
        this.plantillaId = other.optionalPlantillaId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public EventoEquipoCriteria copy() {
        return new EventoEquipoCriteria(this);
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

    public StringFilter getNombreVariable() {
        return nombreVariable;
    }

    public Optional<StringFilter> optionalNombreVariable() {
        return Optional.ofNullable(nombreVariable);
    }

    public StringFilter nombreVariable() {
        if (nombreVariable == null) {
            setNombreVariable(new StringFilter());
        }
        return nombreVariable;
    }

    public void setNombreVariable(StringFilter nombreVariable) {
        this.nombreVariable = nombreVariable;
    }

    public IntegerFilter getDireccionModbus() {
        return direccionModbus;
    }

    public Optional<IntegerFilter> optionalDireccionModbus() {
        return Optional.ofNullable(direccionModbus);
    }

    public IntegerFilter direccionModbus() {
        if (direccionModbus == null) {
            setDireccionModbus(new IntegerFilter());
        }
        return direccionModbus;
    }

    public void setDireccionModbus(IntegerFilter direccionModbus) {
        this.direccionModbus = direccionModbus;
    }

    public TipoRegistroFilter getTipoRegistro() {
        return tipoRegistro;
    }

    public Optional<TipoRegistroFilter> optionalTipoRegistro() {
        return Optional.ofNullable(tipoRegistro);
    }

    public TipoRegistroFilter tipoRegistro() {
        if (tipoRegistro == null) {
            setTipoRegistro(new TipoRegistroFilter());
        }
        return tipoRegistro;
    }

    public void setTipoRegistro(TipoRegistroFilter tipoRegistro) {
        this.tipoRegistro = tipoRegistro;
    }

    public TipoDatoFilter getTipoDato() {
        return tipoDato;
    }

    public Optional<TipoDatoFilter> optionalTipoDato() {
        return Optional.ofNullable(tipoDato);
    }

    public TipoDatoFilter tipoDato() {
        if (tipoDato == null) {
            setTipoDato(new TipoDatoFilter());
        }
        return tipoDato;
    }

    public void setTipoDato(TipoDatoFilter tipoDato) {
        this.tipoDato = tipoDato;
    }

    public BooleanFilter getEsEscribible() {
        return esEscribible;
    }

    public Optional<BooleanFilter> optionalEsEscribible() {
        return Optional.ofNullable(esEscribible);
    }

    public BooleanFilter esEscribible() {
        if (esEscribible == null) {
            setEsEscribible(new BooleanFilter());
        }
        return esEscribible;
    }

    public void setEsEscribible(BooleanFilter esEscribible) {
        this.esEscribible = esEscribible;
    }

    public DoubleFilter getValorNumerico() {
        return valorNumerico;
    }

    public Optional<DoubleFilter> optionalValorNumerico() {
        return Optional.ofNullable(valorNumerico);
    }

    public DoubleFilter valorNumerico() {
        if (valorNumerico == null) {
            setValorNumerico(new DoubleFilter());
        }
        return valorNumerico;
    }

    public void setValorNumerico(DoubleFilter valorNumerico) {
        this.valorNumerico = valorNumerico;
    }

    public BooleanFilter getValorBooleano() {
        return valorBooleano;
    }

    public Optional<BooleanFilter> optionalValorBooleano() {
        return Optional.ofNullable(valorBooleano);
    }

    public BooleanFilter valorBooleano() {
        if (valorBooleano == null) {
            setValorBooleano(new BooleanFilter());
        }
        return valorBooleano;
    }

    public void setValorBooleano(BooleanFilter valorBooleano) {
        this.valorBooleano = valorBooleano;
    }

    public ZonedDateTimeFilter getTimestampActualizacion() {
        return timestampActualizacion;
    }

    public Optional<ZonedDateTimeFilter> optionalTimestampActualizacion() {
        return Optional.ofNullable(timestampActualizacion);
    }

    public ZonedDateTimeFilter timestampActualizacion() {
        if (timestampActualizacion == null) {
            setTimestampActualizacion(new ZonedDateTimeFilter());
        }
        return timestampActualizacion;
    }

    public void setTimestampActualizacion(ZonedDateTimeFilter timestampActualizacion) {
        this.timestampActualizacion = timestampActualizacion;
    }

    public IntegerFilter getIntervaloLectura() {
        return intervaloLectura;
    }

    public Optional<IntegerFilter> optionalIntervaloLectura() {
        return Optional.ofNullable(intervaloLectura);
    }

    public IntegerFilter intervaloLectura() {
        if (intervaloLectura == null) {
            setIntervaloLectura(new IntegerFilter());
        }
        return intervaloLectura;
    }

    public void setIntervaloLectura(IntegerFilter intervaloLectura) {
        this.intervaloLectura = intervaloLectura;
    }

    public DoubleFilter getUmbralAlerta() {
        return umbralAlerta;
    }

    public Optional<DoubleFilter> optionalUmbralAlerta() {
        return Optional.ofNullable(umbralAlerta);
    }

    public DoubleFilter umbralAlerta() {
        if (umbralAlerta == null) {
            setUmbralAlerta(new DoubleFilter());
        }
        return umbralAlerta;
    }

    public void setUmbralAlerta(DoubleFilter umbralAlerta) {
        this.umbralAlerta = umbralAlerta;
    }

    public LongFilter getEquipoId() {
        return equipoId;
    }

    public Optional<LongFilter> optionalEquipoId() {
        return Optional.ofNullable(equipoId);
    }

    public LongFilter equipoId() {
        if (equipoId == null) {
            setEquipoId(new LongFilter());
        }
        return equipoId;
    }

    public void setEquipoId(LongFilter equipoId) {
        this.equipoId = equipoId;
    }

    public LongFilter getPlantillaId() {
        return plantillaId;
    }

    public Optional<LongFilter> optionalPlantillaId() {
        return Optional.ofNullable(plantillaId);
    }

    public LongFilter plantillaId() {
        if (plantillaId == null) {
            setPlantillaId(new LongFilter());
        }
        return plantillaId;
    }

    public void setPlantillaId(LongFilter plantillaId) {
        this.plantillaId = plantillaId;
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
        final EventoEquipoCriteria that = (EventoEquipoCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(nombreVariable, that.nombreVariable) &&
            Objects.equals(direccionModbus, that.direccionModbus) &&
            Objects.equals(tipoRegistro, that.tipoRegistro) &&
            Objects.equals(tipoDato, that.tipoDato) &&
            Objects.equals(esEscribible, that.esEscribible) &&
            Objects.equals(valorNumerico, that.valorNumerico) &&
            Objects.equals(valorBooleano, that.valorBooleano) &&
            Objects.equals(timestampActualizacion, that.timestampActualizacion) &&
            Objects.equals(intervaloLectura, that.intervaloLectura) &&
            Objects.equals(umbralAlerta, that.umbralAlerta) &&
            Objects.equals(equipoId, that.equipoId) &&
            Objects.equals(plantillaId, that.plantillaId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            nombreVariable,
            direccionModbus,
            tipoRegistro,
            tipoDato,
            esEscribible,
            valorNumerico,
            valorBooleano,
            timestampActualizacion,
            intervaloLectura,
            umbralAlerta,
            equipoId,
            plantillaId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EventoEquipoCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNombreVariable().map(f -> "nombreVariable=" + f + ", ").orElse("") +
            optionalDireccionModbus().map(f -> "direccionModbus=" + f + ", ").orElse("") +
            optionalTipoRegistro().map(f -> "tipoRegistro=" + f + ", ").orElse("") +
            optionalTipoDato().map(f -> "tipoDato=" + f + ", ").orElse("") +
            optionalEsEscribible().map(f -> "esEscribible=" + f + ", ").orElse("") +
            optionalValorNumerico().map(f -> "valorNumerico=" + f + ", ").orElse("") +
            optionalValorBooleano().map(f -> "valorBooleano=" + f + ", ").orElse("") +
            optionalTimestampActualizacion().map(f -> "timestampActualizacion=" + f + ", ").orElse("") +
            optionalIntervaloLectura().map(f -> "intervaloLectura=" + f + ", ").orElse("") +
            optionalUmbralAlerta().map(f -> "umbralAlerta=" + f + ", ").orElse("") +
            optionalEquipoId().map(f -> "equipoId=" + f + ", ").orElse("") +
            optionalPlantillaId().map(f -> "plantillaId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
