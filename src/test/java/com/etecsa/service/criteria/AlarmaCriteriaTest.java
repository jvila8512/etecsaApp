package com.etecsa.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class AlarmaCriteriaTest {

    @Test
    void newAlarmaCriteriaHasAllFiltersNullTest() {
        var alarmaCriteria = new AlarmaCriteria();
        assertThat(alarmaCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void alarmaCriteriaFluentMethodsCreatesFiltersTest() {
        var alarmaCriteria = new AlarmaCriteria();

        setAllFilters(alarmaCriteria);

        assertThat(alarmaCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void alarmaCriteriaCopyCreatesNullFilterTest() {
        var alarmaCriteria = new AlarmaCriteria();
        var copy = alarmaCriteria.copy();

        assertThat(alarmaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(alarmaCriteria)
        );
    }

    @Test
    void alarmaCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var alarmaCriteria = new AlarmaCriteria();
        setAllFilters(alarmaCriteria);

        var copy = alarmaCriteria.copy();

        assertThat(alarmaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(alarmaCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var alarmaCriteria = new AlarmaCriteria();

        assertThat(alarmaCriteria).hasToString("AlarmaCriteria{}");
    }

    private static void setAllFilters(AlarmaCriteria alarmaCriteria) {
        alarmaCriteria.id();
        alarmaCriteria.descripcion();
        alarmaCriteria.activatedAt();
        alarmaCriteria.deactivatedAt();
        alarmaCriteria.severidad();
        alarmaCriteria.estado();
        alarmaCriteria.mensajeUsuario();
        alarmaCriteria.eventoId();
        alarmaCriteria.acknowledgedById();
        alarmaCriteria.distinct();
    }

    private static Condition<AlarmaCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getDescripcion()) &&
                condition.apply(criteria.getActivatedAt()) &&
                condition.apply(criteria.getDeactivatedAt()) &&
                condition.apply(criteria.getSeveridad()) &&
                condition.apply(criteria.getEstado()) &&
                condition.apply(criteria.getMensajeUsuario()) &&
                condition.apply(criteria.getEventoId()) &&
                condition.apply(criteria.getAcknowledgedById()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AlarmaCriteria> copyFiltersAre(AlarmaCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getDescripcion(), copy.getDescripcion()) &&
                condition.apply(criteria.getActivatedAt(), copy.getActivatedAt()) &&
                condition.apply(criteria.getDeactivatedAt(), copy.getDeactivatedAt()) &&
                condition.apply(criteria.getSeveridad(), copy.getSeveridad()) &&
                condition.apply(criteria.getEstado(), copy.getEstado()) &&
                condition.apply(criteria.getMensajeUsuario(), copy.getMensajeUsuario()) &&
                condition.apply(criteria.getEventoId(), copy.getEventoId()) &&
                condition.apply(criteria.getAcknowledgedById(), copy.getAcknowledgedById()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
