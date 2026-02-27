package com.etecsa.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class EventoEquipoCriteriaTest {

    @Test
    void newEventoEquipoCriteriaHasAllFiltersNullTest() {
        var eventoEquipoCriteria = new EventoEquipoCriteria();
        assertThat(eventoEquipoCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void eventoEquipoCriteriaFluentMethodsCreatesFiltersTest() {
        var eventoEquipoCriteria = new EventoEquipoCriteria();

        setAllFilters(eventoEquipoCriteria);

        assertThat(eventoEquipoCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void eventoEquipoCriteriaCopyCreatesNullFilterTest() {
        var eventoEquipoCriteria = new EventoEquipoCriteria();
        var copy = eventoEquipoCriteria.copy();

        assertThat(eventoEquipoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(eventoEquipoCriteria)
        );
    }

    @Test
    void eventoEquipoCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var eventoEquipoCriteria = new EventoEquipoCriteria();
        setAllFilters(eventoEquipoCriteria);

        var copy = eventoEquipoCriteria.copy();

        assertThat(eventoEquipoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(eventoEquipoCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var eventoEquipoCriteria = new EventoEquipoCriteria();

        assertThat(eventoEquipoCriteria).hasToString("EventoEquipoCriteria{}");
    }

    private static void setAllFilters(EventoEquipoCriteria eventoEquipoCriteria) {
        eventoEquipoCriteria.id();
        eventoEquipoCriteria.nombreVariable();
        eventoEquipoCriteria.direccionModbus();
        eventoEquipoCriteria.tipoRegistro();
        eventoEquipoCriteria.tipoDato();
        eventoEquipoCriteria.esEscribible();
        eventoEquipoCriteria.valorNumerico();
        eventoEquipoCriteria.valorBooleano();
        eventoEquipoCriteria.timestampActualizacion();
        eventoEquipoCriteria.intervaloLectura();
        eventoEquipoCriteria.umbralAlerta();
        eventoEquipoCriteria.equipoId();
        eventoEquipoCriteria.plantillaId();
        eventoEquipoCriteria.distinct();
    }

    private static Condition<EventoEquipoCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNombreVariable()) &&
                condition.apply(criteria.getDireccionModbus()) &&
                condition.apply(criteria.getTipoRegistro()) &&
                condition.apply(criteria.getTipoDato()) &&
                condition.apply(criteria.getEsEscribible()) &&
                condition.apply(criteria.getValorNumerico()) &&
                condition.apply(criteria.getValorBooleano()) &&
                condition.apply(criteria.getTimestampActualizacion()) &&
                condition.apply(criteria.getIntervaloLectura()) &&
                condition.apply(criteria.getUmbralAlerta()) &&
                condition.apply(criteria.getEquipoId()) &&
                condition.apply(criteria.getPlantillaId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<EventoEquipoCriteria> copyFiltersAre(
        EventoEquipoCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNombreVariable(), copy.getNombreVariable()) &&
                condition.apply(criteria.getDireccionModbus(), copy.getDireccionModbus()) &&
                condition.apply(criteria.getTipoRegistro(), copy.getTipoRegistro()) &&
                condition.apply(criteria.getTipoDato(), copy.getTipoDato()) &&
                condition.apply(criteria.getEsEscribible(), copy.getEsEscribible()) &&
                condition.apply(criteria.getValorNumerico(), copy.getValorNumerico()) &&
                condition.apply(criteria.getValorBooleano(), copy.getValorBooleano()) &&
                condition.apply(criteria.getTimestampActualizacion(), copy.getTimestampActualizacion()) &&
                condition.apply(criteria.getIntervaloLectura(), copy.getIntervaloLectura()) &&
                condition.apply(criteria.getUmbralAlerta(), copy.getUmbralAlerta()) &&
                condition.apply(criteria.getEquipoId(), copy.getEquipoId()) &&
                condition.apply(criteria.getPlantillaId(), copy.getPlantillaId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
