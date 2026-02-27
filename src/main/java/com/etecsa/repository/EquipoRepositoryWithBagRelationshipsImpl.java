package com.etecsa.repository;

import com.etecsa.domain.Equipo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class EquipoRepositoryWithBagRelationshipsImpl implements EquipoRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String EQUIPOS_PARAMETER = "equipos";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Equipo> fetchBagRelationships(Optional<Equipo> equipo) {
        return equipo.map(this::fetchEspecialidades);
    }

    @Override
    public Page<Equipo> fetchBagRelationships(Page<Equipo> equipos) {
        return new PageImpl<>(fetchBagRelationships(equipos.getContent()), equipos.getPageable(), equipos.getTotalElements());
    }

    @Override
    public List<Equipo> fetchBagRelationships(List<Equipo> equipos) {
        return Optional.of(equipos).map(this::fetchEspecialidades).orElse(Collections.emptyList());
    }

    Equipo fetchEspecialidades(Equipo result) {
        return entityManager
            .createQuery("select equipo from Equipo equipo left join fetch equipo.especialidades where equipo.id = :id", Equipo.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<Equipo> fetchEspecialidades(List<Equipo> equipos) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, equipos.size()).forEach(index -> order.put(equipos.get(index).getId(), index));
        List<Equipo> result = entityManager
            .createQuery("select equipo from Equipo equipo left join fetch equipo.especialidades where equipo in :equipos", Equipo.class)
            .setParameter(EQUIPOS_PARAMETER, equipos)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
