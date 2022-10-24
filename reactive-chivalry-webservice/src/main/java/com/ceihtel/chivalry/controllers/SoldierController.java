package com.ceihtel.chivalry.controllers;

import com.ceihtel.chivalry.entities.Soldier;
import com.ceihtel.chivalry.exceptions.SoldierAlreadyExistsException;
import com.ceihtel.chivalry.exceptions.SoldierNotFoundException;
import com.ceihtel.chivalry.mappers.SoldierMapper;
import com.ceihtel.chivalry.repositories.SoldierRepository;
import com.ceihtel.chivalry.requests.CreateSoldierDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/soldiers")
@RequiredArgsConstructor
public class SoldierController {
    private final SoldierRepository soldierRepository;
    private final SoldierMapper soldierMapper;

    public static final String MISSING_SOLDIER = "Could not find a soldier called '%s'";
    public static final String SOLDIER_ALREADY_EXISTS = "A soldier called '%s' already exists";

    @GetMapping
    public Flux<Soldier> getAll() {
        return soldierRepository.findAll();
    }

    @GetMapping("/{name}")
    public Mono<Soldier> getByName(@PathVariable String name) {
        return soldierRepository.findByName(name)
                .switchIfEmpty(Mono.error(new SoldierNotFoundException(String.format(MISSING_SOLDIER, name))));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Soldier> createWithName(@RequestBody @Valid CreateSoldierDTO createSoldierDTO) {
        return soldierRepository.findByName(createSoldierDTO.getName())
                .flatMap(__ -> Mono.error(new SoldierAlreadyExistsException(String.format(SOLDIER_ALREADY_EXISTS, createSoldierDTO.getName()))))
                .switchIfEmpty(Mono.defer(() -> soldierRepository.save(soldierMapper.toEntity(createSoldierDTO))))
                .cast(Soldier.class);
    }

    @PostMapping("/random")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Soldier> createRandom() {
        return soldierRepository.save(new Soldier());
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String name) {
        return soldierRepository.findByName(name)
                .switchIfEmpty(Mono.error(new SoldierNotFoundException(String.format(MISSING_SOLDIER, name))))
                .flatMap(soldierRepository::delete)
                .then(Mono.empty());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAll() {
        return soldierRepository.findAll()
                .flatMap(soldierRepository::delete)
                .then(Mono.empty());
    }
}
