package com.etecsa.repository;

import com.etecsa.domain.EventoEquipo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the EventoEquipo entity.
 */
@Repository
public interface EventoEquipoRepository extends JpaRepository<EventoEquipo, Long>, JpaSpecificationExecutor<EventoEquipo> {
    default Optional<EventoEquipo> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<EventoEquipo> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<EventoEquipo> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select eventoEquipo from EventoEquipo eventoEquipo left join fetch eventoEquipo.equipo left join fetch eventoEquipo.plantilla",
        countQuery = "select count(eventoEquipo) from EventoEquipo eventoEquipo"
    )
    Page<EventoEquipo> findAllWithToOneRelationships(Pageable pageable);

    @Query("select eventoEquipo from EventoEquipo eventoEquipo left join fetch eventoEquipo.equipo left join fetch eventoEquipo.plantilla")
    List<EventoEquipo> findAllWithToOneRelationships();

    @Query(
        "select eventoEquipo from EventoEquipo eventoEquipo left join fetch eventoEquipo.equipo left join fetch eventoEquipo.plantilla where eventoEquipo.id =:id"
    )
    Optional<EventoEquipo> findOneWithToOneRelationships(@Param("id") Long id);

    /**
     * Obtiene los eventos de un equipo ordenados por fecha de actualización (más recientes primero).
     * Usado por el DashboardWebSocketService para enviar el resumen de variables.
     */
    List<EventoEquipo> findByEquipoIdOrderByTimestampActualizacionDesc(Long equipoId);

    @Query("SELECT ee FROM EventoEquipo ee JOIN FETCH ee.plantilla WHERE ee.equipo.id = :equipoId ORDER BY ee.timestampActualizacion DESC")
    List<EventoEquipo> findByEquipoIdWithPlantilla(@Param("equipoId") Long equipoId);
}
