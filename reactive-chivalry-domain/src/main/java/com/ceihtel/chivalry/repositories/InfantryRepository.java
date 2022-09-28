package com.ceihtel.chivalry.repositories;

import com.ceihtel.chivalry.entities.Infantry;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfantryRepository extends ReactiveMongoRepository<Infantry, String> {
}
