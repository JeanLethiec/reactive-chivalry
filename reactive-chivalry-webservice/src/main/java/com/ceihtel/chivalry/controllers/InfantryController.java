package com.ceihtel.chivalry.controllers;

import com.ceihtel.chivalry.entities.Infantry;
import com.ceihtel.chivalry.mappers.InfantryMapper;
import com.ceihtel.chivalry.repositories.InfantryRepository;
import com.ceihtel.chivalry.requests.CreateInfantryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/infantry")
@RequiredArgsConstructor
public class InfantryController {
    private final InfantryRepository infantryRepository;
    private final InfantryMapper infantryMapper;

    @GetMapping("/{name}")
    private Mono<Infantry> getInfantryByName(@PathVariable String name) {
        return infantryRepository.findById(name);
    }

    @PostMapping
    private Mono<Infantry> createInfantry(@RequestBody CreateInfantryDTO createInfantryDTO) {
        return infantryRepository.insert(infantryMapper.toEntity(createInfantryDTO));
    }
}
