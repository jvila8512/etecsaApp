package com.etecsa.repository;

import com.etecsa.domain.Alarma;
import com.etecsa.domain.enumeration.EstadoAlarma;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Alarma entity.
 */
@Repository
public interface AlarmaRepository extends JpaRepository<Alarma, Long>, JpaSpecificationExecutor<Alarma> {
    @Query("select alarma from Alarma alarma where alarma.acknowledgedBy.login = ?#{authentication.name}")
    List<Alarma> findByAcknowledgedByIsCurrentUser();

    default Optional<Alarma> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Alarma> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Alarma> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithEagerRelationships(pageable);
    }

    @Query(
        value = "select alarma from Alarma alarma left join fetch alarma.acknowledgedBy",
        countQuery = "select count(alarma) from Alarma alarma"
    )
    Page<Alarma> findAllWithToOneRelationships(Pageable pageable);

    @Query("select alarma from Alarma alarma left join fetch alarma.acknowledgedBy")
    List<Alarma> findAllWithToOneRelationships();

    @Query("select alarma from Alarma alarma left join fetch alarma.acknowledgedBy where alarma.id =:id")
    Optional<Alarma> findOneWithToOneRelationships(@Param("id") Long id);

    @Query("select alarma from Alarma alarma where alarma.evento.id = :eventoId and alarma.estado in :estados")
    List<Alarma> findByEventoIdAndEstadoIn(@Param("eventoId") Long eventoId, @Param("estados") List<EstadoAlarma> estados);

    @Query("select alarma from Alarma alarma where alarma.evento.id = :eventoId and alarma.estado = :estado")
    Optional<Alarma> findByEventoIdAndEstado(@Param("eventoId") Long eventoId, @Param("estado") EstadoAlarma estado);

    @Query("select count(alarma) from Alarma alarma where alarma.evento.id = :eventoId and alarma.estado in :estados")
    long countByEventoIdAndEstadoIn(@Param("eventoId") Long eventoId, @Param("estados") List<EstadoAlarma> estados);
}
