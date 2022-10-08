package com.ceihtel.chivalry.mappers;

import com.ceihtel.chivalry.entities.Soldier;
import com.ceihtel.chivalry.requests.CreateSoldierDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SoldierMapper {

    Soldier toEntity(CreateSoldierDTO createSoldierDTO);
}
