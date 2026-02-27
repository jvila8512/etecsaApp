package com.etecsa.service.mapper;

import com.etecsa.domain.Equipo;
import com.etecsa.domain.EventoEquipo;
import com.etecsa.domain.EventoPlantilla;
import com.etecsa.service.dto.EquipoDTO;
import com.etecsa.service.dto.EventoEquipoDTO;
import com.etecsa.service.dto.EventoPlantillaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link EventoEquipo} and its DTO {@link EventoEquipoDTO}.
 */
@Mapper(componentModel = "spring")
public interface EventoEquipoMapper extends EntityMapper<EventoEquipoDTO, EventoEquipo> {
    @Mapping(target = "equipo", source = "equipo", qualifiedByName = "equipoNombre")
    @Mapping(target = "plantilla", source = "plantilla", qualifiedByName = "eventoPlantillaNombre")
    EventoEquipoDTO toDto(EventoEquipo s);

    @Named("equipoNombre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    EquipoDTO toDtoEquipoNombre(Equipo equipo);

    @Named("eventoPlantillaNombre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    EventoPlantillaDTO toDtoEventoPlantillaNombre(EventoPlantilla eventoPlantilla);
}
