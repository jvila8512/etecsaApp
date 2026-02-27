package com.etecsa.service.mapper;

import com.etecsa.domain.Alarma;
import com.etecsa.domain.EventoEquipo;
import com.etecsa.domain.User;
import com.etecsa.service.dto.AlarmaDTO;
import com.etecsa.service.dto.EventoEquipoDTO;
import com.etecsa.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Alarma} and its DTO {@link AlarmaDTO}.
 */
@Mapper(componentModel = "spring")
public interface AlarmaMapper extends EntityMapper<AlarmaDTO, Alarma> {
    @Mapping(target = "evento", source = "evento", qualifiedByName = "eventoEquipoId")
    @Mapping(target = "acknowledgedBy", source = "acknowledgedBy", qualifiedByName = "userLogin")
    AlarmaDTO toDto(Alarma s);

    @Named("eventoEquipoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    EventoEquipoDTO toDtoEventoEquipoId(EventoEquipo eventoEquipo);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
