package com.etecsa.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.etecsa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AlarmaDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AlarmaDTO.class);
        AlarmaDTO alarmaDTO1 = new AlarmaDTO();
        alarmaDTO1.setId(1L);
        AlarmaDTO alarmaDTO2 = new AlarmaDTO();
        assertThat(alarmaDTO1).isNotEqualTo(alarmaDTO2);
        alarmaDTO2.setId(alarmaDTO1.getId());
        assertThat(alarmaDTO1).isEqualTo(alarmaDTO2);
        alarmaDTO2.setId(2L);
        assertThat(alarmaDTO1).isNotEqualTo(alarmaDTO2);
        alarmaDTO1.setId(null);
        assertThat(alarmaDTO1).isNotEqualTo(alarmaDTO2);
    }
}
