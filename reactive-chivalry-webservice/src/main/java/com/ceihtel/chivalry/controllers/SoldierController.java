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

    public static final String MISSING_SOLDIER = "Could not find Soldier with id '%s'";
    public static final String SOLDIER_ALREADY_EXISTS = "A soldier called '%s' already exists";

    @GetMapping
    public Flux<Soldier> getAll() {
        return soldierRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Soldier> getById(@PathVariable String id) {
        return soldierRepository.findById(id)
                .switchIfEmpty(Mono.error(new SoldierNotFoundException(String.format(MISSING_SOLDIER, id))));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Soldier> create(@RequestBody @Valid CreateSoldierDTO createSoldierDTO) {
        return soldierRepository.findByName(createSoldierDTO.getName())
                .flatMap(__ -> Mono.error(new SoldierAlreadyExistsException(String.format(SOLDIER_ALREADY_EXISTS, createSoldierDTO.getName()))))
                .switchIfEmpty(Mono.defer(() -> soldierRepository.save(soldierMapper.toEntity(createSoldierDTO))))
                .cast(Soldier.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return soldierRepository.findById(id)
                .doOnNext(soldierRepository::delete)
                .switchIfEmpty(Mono.error(new SoldierNotFoundException(String.format(MISSING_SOLDIER, id))))
                .flatMap(x -> Mono.empty());
    }
}
