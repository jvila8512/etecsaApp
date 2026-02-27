package com.etecsa.service.mapper;

import com.etecsa.domain.Equipo;
import com.etecsa.domain.Especialidad;
import com.etecsa.service.dto.EquipoDTO;
import com.etecsa.service.dto.EspecialidadDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Especialidad} and its DTO {@link EspecialidadDTO}.
 */
@Mapper(componentModel = "spring")
public interface EspecialidadMapper extends EntityMapper<EspecialidadDTO, Especialidad> {
    @Mapping(target = "equipos", source = "equipos", qualifiedByName = "equipoIdSet")
    EspecialidadDTO toDto(Especialidad s);

    @Mapping(target = "equipos", ignore = true)
    @Mapping(target = "removeEquipos", ignore = true)
    Especialidad toEntity(EspecialidadDTO especialidadDTO);

    @Named("equipoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    EquipoDTO toDtoEquipoId(Equipo equipo);

    @Named("equipoIdSet")
    default Set<EquipoDTO> toDtoEquipoIdSet(Set<Equipo> equipo) {
        return equipo.stream().map(this::toDtoEquipoId).collect(Collectors.toSet());
    }
}
