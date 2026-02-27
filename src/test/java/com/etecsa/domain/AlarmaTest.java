package com.etecsa.domain;

import static com.etecsa.domain.AlarmaTestSamples.*;
import static com.etecsa.domain.EventoEquipoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.etecsa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlarmaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Alarma.class);
        Alarma alarma1 = getAlarmaSample1();
        Alarma alarma2 = new Alarma();
        assertThat(alarma1).isNotEqualTo(alarma2);

        alarma2.setId(alarma1.getId());
        assertThat(alarma1).isEqualTo(alarma2);

        alarma2 = getAlarmaSample2();
        assertThat(alarma1).isNotEqualTo(alarma2);
    }

    @Test
    void eventoTest() {
        Alarma alarma = getAlarmaRandomSampleGenerator();
        EventoEquipo eventoEquipoBack = getEventoEquipoRandomSampleGenerator();

        alarma.setEvento(eventoEquipoBack);
        assertThat(alarma.getEvento()).isEqualTo(eventoEquipoBack);

        alarma.evento(null);
        assertThat(alarma.getEvento()).isNull();
    }
}
