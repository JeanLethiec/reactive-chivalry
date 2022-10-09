package com.ceihtel.chivalry.repositories;

import com.ceihtel.chivalry.entities.Soldier;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoldierRepository extends ReactiveMongoRepository<Soldier, String> {
}
