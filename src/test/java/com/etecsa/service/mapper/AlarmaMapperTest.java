package com.etecsa.service.mapper;

import static com.etecsa.domain.AlarmaAsserts.*;
import static com.etecsa.domain.AlarmaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlarmaMapperTest {

    private AlarmaMapper alarmaMapper;

    @BeforeEach
    void setUp() {
        alarmaMapper = new AlarmaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAlarmaSample1();
        var actual = alarmaMapper.toEntity(alarmaMapper.toDto(expected));
        assertAlarmaAllPropertiesEquals(expected, actual);
    }
}
