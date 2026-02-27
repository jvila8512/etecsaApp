package com.etecsa.domain;

import static com.etecsa.domain.EquipoTestSamples.*;
import static com.etecsa.domain.EventoEquipoTestSamples.*;
import static com.etecsa.domain.EventoPlantillaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.etecsa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EventoEquipoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(EventoEquipo.class);
        EventoEquipo eventoEquipo1 = getEventoEquipoSample1();
        EventoEquipo eventoEquipo2 = new EventoEquipo();
        assertThat(eventoEquipo1).isNotEqualTo(eventoEquipo2);

        eventoEquipo2.setId(eventoEquipo1.getId());
        assertThat(eventoEquipo1).isEqualTo(eventoEquipo2);

        eventoEquipo2 = getEventoEquipoSample2();
        assertThat(eventoEquipo1).isNotEqualTo(eventoEquipo2);
    }

    @Test
    void equipoTest() {
        EventoEquipo eventoEquipo = getEventoEquipoRandomSampleGenerator();
        Equipo equipoBack = getEquipoRandomSampleGenerator();

        eventoEquipo.setEquipo(equipoBack);
        assertThat(eventoEquipo.getEquipo()).isEqualTo(equipoBack);

        eventoEquipo.equipo(null);
        assertThat(eventoEquipo.getEquipo()).isNull();
    }

    @Test
    void plantillaTest() {
        EventoEquipo eventoEquipo = getEventoEquipoRandomSampleGenerator();
        EventoPlantilla eventoPlantillaBack = getEventoPlantillaRandomSampleGenerator();

        eventoEquipo.setPlantilla(eventoPlantillaBack);
        assertThat(eventoEquipo.getPlantilla()).isEqualTo(eventoPlantillaBack);

        eventoEquipo.plantilla(null);
        assertThat(eventoEquipo.getPlantilla()).isNull();
    }
}
