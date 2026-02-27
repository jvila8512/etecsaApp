package com.etecsa.domain;

import static com.etecsa.domain.EquipoTestSamples.*;
import static com.etecsa.domain.EspecialidadTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.etecsa.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EspecialidadTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Especialidad.class);
        Especialidad especialidad1 = getEspecialidadSample1();
        Especialidad especialidad2 = new Especialidad();
        assertThat(especialidad1).isNotEqualTo(especialidad2);

        especialidad2.setId(especialidad1.getId());
        assertThat(especialidad1).isEqualTo(especialidad2);

        especialidad2 = getEspecialidadSample2();
        assertThat(especialidad1).isNotEqualTo(especialidad2);
    }

    @Test
    void equiposTest() {
        Especialidad especialidad = getEspecialidadRandomSampleGenerator();
        Equipo equipoBack = getEquipoRandomSampleGenerator();

        especialidad.addEquipos(equipoBack);
        assertThat(especialidad.getEquipos()).containsOnly(equipoBack);
        assertThat(equipoBack.getEspecialidades()).containsOnly(especialidad);

        especialidad.removeEquipos(equipoBack);
        assertThat(especialidad.getEquipos()).doesNotContain(equipoBack);
        assertThat(equipoBack.getEspecialidades()).doesNotContain(especialidad);

        especialidad.equipos(new HashSet<>(Set.of(equipoBack)));
        assertThat(especialidad.getEquipos()).containsOnly(equipoBack);
        assertThat(equipoBack.getEspecialidades()).containsOnly(especialidad);

        especialidad.setEquipos(new HashSet<>());
        assertThat(especialidad.getEquipos()).doesNotContain(equipoBack);
        assertThat(equipoBack.getEspecialidades()).doesNotContain(especialidad);
    }
}
