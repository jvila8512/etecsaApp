package com.etecsa.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class SitioCriteriaTest {

    @Test
    void newSitioCriteriaHasAllFiltersNullTest() {
        var sitioCriteria = new SitioCriteria();
        assertThat(sitioCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void sitioCriteriaFluentMethodsCreatesFiltersTest() {
        var sitioCriteria = new SitioCriteria();

        setAllFilters(sitioCriteria);

        assertThat(sitioCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void sitioCriteriaCopyCreatesNullFilterTest() {
        var sitioCriteria = new SitioCriteria();
        var copy = sitioCriteria.copy();

        assertThat(sitioCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(sitioCriteria)
        );
    }

    @Test
    void sitioCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var sitioCriteria = new SitioCriteria();
        setAllFilters(sitioCriteria);

        var copy = sitioCriteria.copy();

        assertThat(sitioCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(sitioCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var sitioCriteria = new SitioCriteria();

        assertThat(sitioCriteria).hasToString("SitioCriteria{}");
    }

    private static void setAllFilters(SitioCriteria sitioCriteria) {
        sitioCriteria.id();
        sitioCriteria.nombre();
        sitioCriteria.codigo();
        sitioCriteria.ubicacion();
        sitioCriteria.fechaRegistro();
        sitioCriteria.distinct();
    }

    private static Condition<SitioCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNombre()) &&
                condition.apply(criteria.getCodigo()) &&
                condition.apply(criteria.getUbicacion()) &&
                condition.apply(criteria.getFechaRegistro()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<SitioCriteria> copyFiltersAre(SitioCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNombre(), copy.getNombre()) &&
                condition.apply(criteria.getCodigo(), copy.getCodigo()) &&
                condition.apply(criteria.getUbicacion(), copy.getUbicacion()) &&
                condition.apply(criteria.getFechaRegistro(), copy.getFechaRegistro()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
