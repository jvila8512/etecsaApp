package com.etecsa.repository;

import com.etecsa.domain.Alarma;
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
        return this.findAllWithToOneRelationships(pageable);
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
}
