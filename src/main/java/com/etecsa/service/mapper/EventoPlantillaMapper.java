package com.etecsa.service.mapper;

import com.etecsa.domain.Especialidad;
import com.etecsa.domain.EventoPlantilla;
import com.etecsa.service.dto.EspecialidadDTO;
import com.etecsa.service.dto.EventoPlantillaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link EventoPlantilla} and its DTO {@link EventoPlantillaDTO}.
 */
@Mapper(componentModel = "spring")
public interface EventoPlantillaMapper extends EntityMapper<EventoPlantillaDTO, EventoPlantilla> {
    @Mapping(target = "especialidad", source = "especialidad", qualifiedByName = "especialidadNombre")
    EventoPlantillaDTO toDto(EventoPlantilla s);

    @Named("especialidadNombre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    EspecialidadDTO toDtoEspecialidadNombre(Especialidad especialidad);
}
