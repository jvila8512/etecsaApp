package com.etecsa.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.etecsa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SitioDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SitioDTO.class);
        SitioDTO sitioDTO1 = new SitioDTO();
        sitioDTO1.setId(1L);
        SitioDTO sitioDTO2 = new SitioDTO();
        assertThat(sitioDTO1).isNotEqualTo(sitioDTO2);
        sitioDTO2.setId(sitioDTO1.getId());
        assertThat(sitioDTO1).isEqualTo(sitioDTO2);
        sitioDTO2.setId(2L);
        assertThat(sitioDTO1).isNotEqualTo(sitioDTO2);
        sitioDTO1.setId(null);
        assertThat(sitioDTO1).isNotEqualTo(sitioDTO2);
    }
}
