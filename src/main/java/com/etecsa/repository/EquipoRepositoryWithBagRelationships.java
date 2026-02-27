package com.etecsa.repository;

import com.etecsa.domain.Equipo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface EquipoRepositoryWithBagRelationships {
    Optional<Equipo> fetchBagRelationships(Optional<Equipo> equipo);

    List<Equipo> fetchBagRelationships(List<Equipo> equipos);

    Page<Equipo> fetchBagRelationships(Page<Equipo> equipos);
}
