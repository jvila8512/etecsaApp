package com.etecsa.service.mapper;

import com.etecsa.domain.Equipo;
import com.etecsa.domain.Especialidad;
import com.etecsa.domain.Sitio;
import com.etecsa.service.dto.EquipoDTO;
import com.etecsa.service.dto.EspecialidadDTO;
import com.etecsa.service.dto.SitioDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Equipo} and its DTO {@link EquipoDTO}.
 */
@Mapper(componentModel = "spring")
public interface EquipoMapper extends EntityMapper<EquipoDTO, Equipo> {
    @Mapping(target = "sitio", source = "sitio", qualifiedByName = "sitioNombre")
    @Mapping(target = "especialidades", source = "especialidades", qualifiedByName = "especialidadNombreSet")
    EquipoDTO toDto(Equipo s);

    @Mapping(target = "removeEspecialidades", ignore = true)
    Equipo toEntity(EquipoDTO equipoDTO);

    @Named("sitioNombre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    SitioDTO toDtoSitioNombre(Sitio sitio);

    @Named("especialidadNombre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    EspecialidadDTO toDtoEspecialidadNombre(Especialidad especialidad);

    @Named("especialidadNombreSet")
    default Set<EspecialidadDTO> toDtoEspecialidadNombreSet(Set<Especialidad> especialidad) {
        return especialidad.stream().map(this::toDtoEspecialidadNombre).collect(Collectors.toSet());
    }
}
