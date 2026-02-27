package com.etecsa.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.etecsa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EventoEquipoDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(EventoEquipoDTO.class);
        EventoEquipoDTO eventoEquipoDTO1 = new EventoEquipoDTO();
        eventoEquipoDTO1.setId(1L);
        EventoEquipoDTO eventoEquipoDTO2 = new EventoEquipoDTO();
        assertThat(eventoEquipoDTO1).isNotEqualTo(eventoEquipoDTO2);
        eventoEquipoDTO2.setId(eventoEquipoDTO1.getId());
        assertThat(eventoEquipoDTO1).isEqualTo(eventoEquipoDTO2);
        eventoEquipoDTO2.setId(2L);
        assertThat(eventoEquipoDTO1).isNotEqualTo(eventoEquipoDTO2);
        eventoEquipoDTO1.setId(null);
        assertThat(eventoEquipoDTO1).isNotEqualTo(eventoEquipoDTO2);
    }
}
