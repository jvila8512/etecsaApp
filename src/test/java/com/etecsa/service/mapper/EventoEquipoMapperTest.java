package com.etecsa.service.mapper;

import static com.etecsa.domain.EventoEquipoAsserts.*;
import static com.etecsa.domain.EventoEquipoTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventoEquipoMapperTest {

    private EventoEquipoMapper eventoEquipoMapper;

    @BeforeEach
    void setUp() {
        eventoEquipoMapper = new EventoEquipoMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getEventoEquipoSample1();
        var actual = eventoEquipoMapper.toEntity(eventoEquipoMapper.toDto(expected));
        assertEventoEquipoAllPropertiesEquals(expected, actual);
    }
}
