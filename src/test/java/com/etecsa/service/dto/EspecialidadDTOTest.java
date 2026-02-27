package com.etecsa.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.etecsa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EspecialidadDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(EspecialidadDTO.class);
        EspecialidadDTO especialidadDTO1 = new EspecialidadDTO();
        especialidadDTO1.setId(1L);
        EspecialidadDTO especialidadDTO2 = new EspecialidadDTO();
        assertThat(especialidadDTO1).isNotEqualTo(especialidadDTO2);
        especialidadDTO2.setId(especialidadDTO1.getId());
        assertThat(especialidadDTO1).isEqualTo(especialidadDTO2);
        especialidadDTO2.setId(2L);
        assertThat(especialidadDTO1).isNotEqualTo(especialidadDTO2);
        especialidadDTO1.setId(null);
        assertThat(especialidadDTO1).isNotEqualTo(especialidadDTO2);
    }
}
