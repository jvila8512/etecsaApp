package com.etecsa.service.mapper;

import static com.etecsa.domain.EquipoAsserts.*;
import static com.etecsa.domain.EquipoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EquipoMapperTest {

    private EquipoMapper equipoMapper;

    @BeforeEach
    void setUp() {
        equipoMapper = new EquipoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getEquipoSample1();
        var actual = equipoMapper.toEntity(equipoMapper.toDto(expected));
        assertEquipoAllPropertiesEquals(expected, actual);
    }
}
