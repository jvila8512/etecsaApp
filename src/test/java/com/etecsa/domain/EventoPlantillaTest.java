package com.etecsa.domain;

import static com.etecsa.domain.EspecialidadTestSamples.*;
import static com.etecsa.domain.EventoPlantillaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.etecsa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EventoPlantillaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(EventoPlantilla.class);
        EventoPlantilla eventoPlantilla1 = getEventoPlantillaSample1();
        EventoPlantilla eventoPlantilla2 = new EventoPlantilla();
        assertThat(eventoPlantilla1).isNotEqualTo(eventoPlantilla2);

        eventoPlantilla2.setId(eventoPlantilla1.getId());
        assertThat(eventoPlantilla1).isEqualTo(eventoPlantilla2);

        eventoPlantilla2 = getEventoPlantillaSample2();
        assertThat(eventoPlantilla1).isNotEqualTo(eventoPlantilla2);
    }

    @Test
    void especialidadTest() {
        EventoPlantilla eventoPlantilla = getEventoPlantillaRandomSampleGenerator();
        Especialidad especialidadBack = getEspecialidadRandomSampleGenerator();

        eventoPlantilla.setEspecialidad(especialidadBack);
        assertThat(eventoPlantilla.getEspecialidad()).isEqualTo(especialidadBack);

        eventoPlantilla.especialidad(null);
        assertThat(eventoPlantilla.getEspecialidad()).isNull();
    }
}
