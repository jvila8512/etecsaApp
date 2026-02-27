package com.etecsa.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class EspecialidadCriteriaTest {

    @Test
    void newEspecialidadCriteriaHasAllFiltersNullTest() {
        var especialidadCriteria = new EspecialidadCriteria();
        assertThat(especialidadCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void especialidadCriteriaFluentMethodsCreatesFiltersTest() {
        var especialidadCriteria = new EspecialidadCriteria();

        setAllFilters(especialidadCriteria);

        assertThat(especialidadCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void especialidadCriteriaCopyCreatesNullFilterTest() {
        var especialidadCriteria = new EspecialidadCriteria();
        var copy = especialidadCriteria.copy();

        assertThat(especialidadCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(especialidadCriteria)
        );
    }

    @Test
    void especialidadCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var especialidadCriteria = new EspecialidadCriteria();
        setAllFilters(especialidadCriteria);

        var copy = especialidadCriteria.copy();

        assertThat(especialidadCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(especialidadCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var especialidadCriteria = new EspecialidadCriteria();

        assertThat(especialidadCriteria).hasToString("EspecialidadCriteria{}");
    }

    private static void setAllFilters(EspecialidadCriteria especialidadCriteria) {
        especialidadCriteria.id();
        especialidadCriteria.nombre();
        especialidadCriteria.codigo();
        especialidadCriteria.descripcionTecnica();
        especialidadCriteria.equiposId();
        especialidadCriteria.distinct();
    }

    private static Condition<EspecialidadCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNombre()) &&
                condition.apply(criteria.getCodigo()) &&
                condition.apply(criteria.getDescripcionTecnica()) &&
                condition.apply(criteria.getEquiposId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<EspecialidadCriteria> copyFiltersAre(
        EspecialidadCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNombre(), copy.getNombre()) &&
                condition.apply(criteria.getCodigo(), copy.getCodigo()) &&
                condition.apply(criteria.getDescripcionTecnica(), copy.getDescripcionTecnica()) &&
                condition.apply(criteria.getEquiposId(), copy.getEquiposId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
