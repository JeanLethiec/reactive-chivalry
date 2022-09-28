package com.ceihtel.chivalry.mappers;

import com.ceihtel.chivalry.entities.Infantry;
import com.ceihtel.chivalry.requests.CreateInfantryDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InfantryMapper {

    Infantry toEntity(CreateInfantryDTO createInfantryDTO);
}
