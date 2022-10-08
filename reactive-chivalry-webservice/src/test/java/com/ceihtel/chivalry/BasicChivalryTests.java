package com.ceihtel.chivalry;

import com.ceihtel.chivalry.entities.Soldier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class BasicChivalryTests {
    @Test
    void single() {
        Soldier soldier = getSoldier();
        Mono<Soldier> army = Mono.just(soldier);

        List<Soldier> soldiers = new ArrayList<>();

        army.log().subscribe(soldiers::add);

        assertThat(soldiers).containsExactly(soldier);
    }

    @Test
    void multi() {
        Soldier soldier1 = getSoldier();
        Soldier soldier2 = getSoldier();
        Soldier soldier3 = getSoldier();
        Flux<Soldier> army = Flux.just(soldier1, soldier2, soldier3);

        List<Soldier> soldiers = new ArrayList<>();

        army.log().subscribe(x -> {
            log.info(String.format("Behold ! %s is joining the fight !", x.getName()));
            soldiers.add(x);
        });

        assertThat(soldiers).containsExactly(soldier1, soldier2, soldier3);
    }

    private static Soldier getSoldier() {
        var soldier = new Soldier();
        soldier.setId(RandomUtils.nextLong());
        soldier.setName("Roger");
        soldier.setWeapon("Crossbow");
        return soldier;
    }
}
