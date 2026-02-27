package com.etecsa.repository;

import com.etecsa.domain.EventoPlantilla;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the EventoPlantilla entity.
 */
@Repository
public interface EventoPlantillaRepository extends JpaRepository<EventoPlantilla, Long>, JpaSpecificationExecutor<EventoPlantilla> {
    default Optional<EventoPlantilla> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<EventoPlantilla> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<EventoPlantilla> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select eventoPlantilla from EventoPlantilla eventoPlantilla left join fetch eventoPlantilla.especialidad",
        countQuery = "select count(eventoPlantilla) from EventoPlantilla eventoPlantilla"
    )
    Page<EventoPlantilla> findAllWithToOneRelationships(Pageable pageable);

    @Query("select eventoPlantilla from EventoPlantilla eventoPlantilla left join fetch eventoPlantilla.especialidad")
    List<EventoPlantilla> findAllWithToOneRelationships();

    @Query(
        "select eventoPlantilla from EventoPlantilla eventoPlantilla left join fetch eventoPlantilla.especialidad where eventoPlantilla.id =:id"
    )
    Optional<EventoPlantilla> findOneWithToOneRelationships(@Param("id") Long id);
}
