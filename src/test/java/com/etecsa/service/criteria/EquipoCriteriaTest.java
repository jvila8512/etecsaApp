package com.etecsa.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class EquipoCriteriaTest {

    @Test
    void newEquipoCriteriaHasAllFiltersNullTest() {
        var equipoCriteria = new EquipoCriteria();
        assertThat(equipoCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void equipoCriteriaFluentMethodsCreatesFiltersTest() {
        var equipoCriteria = new EquipoCriteria();

        setAllFilters(equipoCriteria);

        assertThat(equipoCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void equipoCriteriaCopyCreatesNullFilterTest() {
        var equipoCriteria = new EquipoCriteria();
        var copy = equipoCriteria.copy();

        assertThat(equipoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(equipoCriteria)
        );
    }

    @Test
    void equipoCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var equipoCriteria = new EquipoCriteria();
        setAllFilters(equipoCriteria);

        var copy = equipoCriteria.copy();

        assertThat(equipoCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(equipoCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var equipoCriteria = new EquipoCriteria();

        assertThat(equipoCriteria).hasToString("EquipoCriteria{}");
    }

    private static void setAllFilters(EquipoCriteria equipoCriteria) {
        equipoCriteria.id();
        equipoCriteria.nombre();
        equipoCriteria.direccionIp();
        equipoCriteria.modbusSlaveId();
        equipoCriteria.modelo();
        equipoCriteria.firmwareVersion();
        equipoCriteria.estado();
        equipoCriteria.ultimoHeartbeat();
        equipoCriteria.sitioId();
        equipoCriteria.especialidadesId();
        equipoCriteria.distinct();
    }

    private static Condition<EquipoCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNombre()) &&
                condition.apply(criteria.getDireccionIp()) &&
                condition.apply(criteria.getModbusSlaveId()) &&
                condition.apply(criteria.getModelo()) &&
                condition.apply(criteria.getFirmwareVersion()) &&
                condition.apply(criteria.getEstado()) &&
                condition.apply(criteria.getUltimoHeartbeat()) &&
                condition.apply(criteria.getSitioId()) &&
                condition.apply(criteria.getEspecialidadesId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<EquipoCriteria> copyFiltersAre(EquipoCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNombre(), copy.getNombre()) &&
                condition.apply(criteria.getDireccionIp(), copy.getDireccionIp()) &&
                condition.apply(criteria.getModbusSlaveId(), copy.getModbusSlaveId()) &&
                condition.apply(criteria.getModelo(), copy.getModelo()) &&
                condition.apply(criteria.getFirmwareVersion(), copy.getFirmwareVersion()) &&
                condition.apply(criteria.getEstado(), copy.getEstado()) &&
                condition.apply(criteria.getUltimoHeartbeat(), copy.getUltimoHeartbeat()) &&
                condition.apply(criteria.getSitioId(), copy.getSitioId()) &&
                condition.apply(criteria.getEspecialidadesId(), copy.getEspecialidadesId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
