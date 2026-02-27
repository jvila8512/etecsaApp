package com.etecsa.repository;

import com.etecsa.domain.Equipo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Equipo entity.
 *
 * When extending this class, extend EquipoRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface EquipoRepository
    extends EquipoRepositoryWithBagRelationships, JpaRepository<Equipo, Long>, JpaSpecificationExecutor<Equipo> {
    default Optional<Equipo> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findOneWithToOneRelationships(id));
    }

    default List<Equipo> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships());
    }

    default Page<Equipo> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships(pageable));
    }

    @Query(value = "select equipo from Equipo equipo left join fetch equipo.sitio", countQuery = "select count(equipo) from Equipo equipo")
    Page<Equipo> findAllWithToOneRelationships(Pageable pageable);

    @Query("select equipo from Equipo equipo left join fetch equipo.sitio")
    List<Equipo> findAllWithToOneRelationships();

    @Query("select equipo from Equipo equipo left join fetch equipo.sitio where equipo.id =:id")
    Optional<Equipo> findOneWithToOneRelationships(@Param("id") Long id);
}
