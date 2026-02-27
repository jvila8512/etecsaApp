package com.etecsa.service.mapper;

import com.etecsa.domain.Sitio;
import com.etecsa.service.dto.SitioDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Sitio} and its DTO {@link SitioDTO}.
 */
@Mapper(componentModel = "spring")
public interface SitioMapper extends EntityMapper<SitioDTO, Sitio> {}
