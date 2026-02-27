package com.etecsa.service.mapper;

import static com.etecsa.domain.EventoPlantillaAsserts.*;
import static com.etecsa.domain.EventoPlantillaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventoPlantillaMapperTest {

    private EventoPlantillaMapper eventoPlantillaMapper;

    @BeforeEach
    void setUp() {
        eventoPlantillaMapper = new EventoPlantillaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getEventoPlantillaSample1();
        var actual = eventoPlantillaMapper.toEntity(eventoPlantillaMapper.toDto(expected));
        assertEventoPlantillaAllPropertiesEquals(expected, actual);
    }
}
