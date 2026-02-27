package com.etecsa.domain;

import static com.etecsa.domain.EquipoTestSamples.*;
import static com.etecsa.domain.EspecialidadTestSamples.*;
import static com.etecsa.domain.SitioTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.etecsa.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EquipoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Equipo.class);
        Equipo equipo1 = getEquipoSample1();
        Equipo equipo2 = new Equipo();
        assertThat(equipo1).isNotEqualTo(equipo2);

        equipo2.setId(equipo1.getId());
        assertThat(equipo1).isEqualTo(equipo2);

        equipo2 = getEquipoSample2();
        assertThat(equipo1).isNotEqualTo(equipo2);
    }

    @Test
    void sitioTest() {
        Equipo equipo = getEquipoRandomSampleGenerator();
        Sitio sitioBack = getSitioRandomSampleGenerator();

        equipo.setSitio(sitioBack);
        assertThat(equipo.getSitio()).isEqualTo(sitioBack);

        equipo.sitio(null);
        assertThat(equipo.getSitio()).isNull();
    }

    @Test
    void especialidadesTest() {
        Equipo equipo = getEquipoRandomSampleGenerator();
        Especialidad especialidadBack = getEspecialidadRandomSampleGenerator();

        equipo.addEspecialidades(especialidadBack);
        assertThat(equipo.getEspecialidades()).containsOnly(especialidadBack);

        equipo.removeEspecialidades(especialidadBack);
        assertThat(equipo.getEspecialidades()).doesNotContain(especialidadBack);

        equipo.especialidades(new HashSet<>(Set.of(especialidadBack)));
        assertThat(equipo.getEspecialidades()).containsOnly(especialidadBack);

        equipo.setEspecialidades(new HashSet<>());
        assertThat(equipo.getEspecialidades()).doesNotContain(especialidadBack);
    }
}
