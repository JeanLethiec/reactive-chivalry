package com.ceihtel.chivalry.controllers;

import com.ceihtel.chivalry.entities.Soldier;
import com.ceihtel.chivalry.mappers.SoldierMapper;
import com.ceihtel.chivalry.repositories.SoldierRepository;
import com.ceihtel.chivalry.requests.CreateSoldierDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/soldiers")
@RequiredArgsConstructor
public class SoldierController {
    private final SoldierRepository soldierRepository;
    private final SoldierMapper soldierMapper;

    @GetMapping
    private Flux<Soldier> getAll() {
        return soldierRepository.findAll();
    }

    @GetMapping("/{id}")
    private Mono<Soldier> getById(@PathVariable Long id) {
        Mono<Soldier> soldier = soldierRepository.findById(id);
        return soldier;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private Mono<Soldier> create(@RequestBody CreateSoldierDTO createSoldierDTO) {
        return soldierRepository.insert(soldierMapper.toEntity(createSoldierDTO));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private void delete(@PathVariable Long id) {
        soldierRepository.deleteById(id);
    }
}