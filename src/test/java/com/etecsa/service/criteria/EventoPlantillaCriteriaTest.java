package com.etecsa.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class EventoPlantillaCriteriaTest {

    @Test
    void newEventoPlantillaCriteriaHasAllFiltersNullTest() {
        var eventoPlantillaCriteria = new EventoPlantillaCriteria();
        assertThat(eventoPlantillaCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void eventoPlantillaCriteriaFluentMethodsCreatesFiltersTest() {
        var eventoPlantillaCriteria = new EventoPlantillaCriteria();

        setAllFilters(eventoPlantillaCriteria);

        assertThat(eventoPlantillaCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void eventoPlantillaCriteriaCopyCreatesNullFilterTest() {
        var eventoPlantillaCriteria = new EventoPlantillaCriteria();
        var copy = eventoPlantillaCriteria.copy();

        assertThat(eventoPlantillaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(eventoPlantillaCriteria)
        );
    }

    @Test
    void eventoPlantillaCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var eventoPlantillaCriteria = new EventoPlantillaCriteria();
        setAllFilters(eventoPlantillaCriteria);

        var copy = eventoPlantillaCriteria.copy();

        assertThat(eventoPlantillaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(eventoPlantillaCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var eventoPlantillaCriteria = new EventoPlantillaCriteria();

        assertThat(eventoPlantillaCriteria).hasToString("EventoPlantillaCriteria{}");
    }

    private static void setAllFilters(EventoPlantillaCriteria eventoPlantillaCriteria) {
        eventoPlantillaCriteria.id();
        eventoPlantillaCriteria.nombre();
        eventoPlantillaCriteria.descripcion();
        eventoPlantillaCriteria.scalingFactor();
        eventoPlantillaCriteria.unidadMedida();
        eventoPlantillaCriteria.funcionLectura();
        eventoPlantillaCriteria.funcionEscritura();
        eventoPlantillaCriteria.especialidadId();
        eventoPlantillaCriteria.distinct();
    }

    private static Condition<EventoPlantillaCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNombre()) &&
                condition.apply(criteria.getDescripcion()) &&
                condition.apply(criteria.getScalingFactor()) &&
                condition.apply(criteria.getUnidadMedida()) &&
                condition.apply(criteria.getFuncionLectura()) &&
                condition.apply(criteria.getFuncionEscritura()) &&
                condition.apply(criteria.getEspecialidadId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<EventoPlantillaCriteria> copyFiltersAre(
        EventoPlantillaCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNombre(), copy.getNombre()) &&
                condition.apply(criteria.getDescripcion(), copy.getDescripcion()) &&
                condition.apply(criteria.getScalingFactor(), copy.getScalingFactor()) &&
                condition.apply(criteria.getUnidadMedida(), copy.getUnidadMedida()) &&
                condition.apply(criteria.getFuncionLectura(), copy.getFuncionLectura()) &&
                condition.apply(criteria.getFuncionEscritura(), copy.getFuncionEscritura()) &&
                condition.apply(criteria.getEspecialidadId(), copy.getEspecialidadId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
