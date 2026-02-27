package com.etecsa.repository;

import com.etecsa.domain.Sitio;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Sitio entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SitioRepository extends JpaRepository<Sitio, Long>, JpaSpecificationExecutor<Sitio> {}
