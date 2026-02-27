package com.etecsa.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.etecsa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EventoPlantillaDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(EventoPlantillaDTO.class);
        EventoPlantillaDTO eventoPlantillaDTO1 = new EventoPlantillaDTO();
        eventoPlantillaDTO1.setId(1L);
        EventoPlantillaDTO eventoPlantillaDTO2 = new EventoPlantillaDTO();
        assertThat(eventoPlantillaDTO1).isNotEqualTo(eventoPlantillaDTO2);
        eventoPlantillaDTO2.setId(eventoPlantillaDTO1.getId());
        assertThat(eventoPlantillaDTO1).isEqualTo(eventoPlantillaDTO2);
        eventoPlantillaDTO2.setId(2L);
        assertThat(eventoPlantillaDTO1).isNotEqualTo(eventoPlantillaDTO2);
        eventoPlantillaDTO1.setId(null);
        assertThat(eventoPlantillaDTO1).isNotEqualTo(eventoPlantillaDTO2);
    }
}
