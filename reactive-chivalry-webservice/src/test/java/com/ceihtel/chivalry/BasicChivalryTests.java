package com.ceihtel.chivalry;

import com.ceihtel.chivalry.entities.Soldier;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @Test
    void multi_delayed() {
        Soldier soldier1 = getSoldier();
        Soldier soldier2 = getSoldier();
        Soldier soldier3 = getSoldier();
        Soldier soldier4 = getSoldier();
        Soldier soldier5 = getSoldier();
        Soldier soldier6 = getSoldier();
        Flux<Soldier> army = Flux.just(soldier1, soldier2, soldier3, soldier4, soldier5, soldier6);

        List<Soldier> soldiers = new ArrayList<>();

        army.log().subscribe(new Subscriber<>() {
            private Subscription s;
            int onNextAmount;

            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                s.request(2);
            }

            @Override
            public void onNext(Soldier soldier) {
                log.info(String.format("Behold ! %s is joining the fight !", soldier.getName()));
                soldiers.add(soldier);
                onNextAmount++;
                if (onNextAmount % 2 == 0) {
                    s.request(2);
                }
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });

        assertThat(soldiers).containsExactly(soldier1, soldier2, soldier3, soldier4, soldier5, soldier6);
    }

    @Disabled("No end condition")
    @Test
    void infinite() {
        ConnectableFlux<Object> publish = Flux.create(fluxSink -> {
                    while (true) {
                        fluxSink.next(getSoldier());
                    }
                })
                .sample(Duration.ofSeconds(1)).publish();


        publish.subscribe(System.out::println);
        publish.subscribe(System.out::println);

        publish.connect();
    }

    @Test
    void concurrency() {
        var soldiers = new ArrayList<Soldier>();

        Flux.just(getSoldier(), getSoldier(), getSoldier(), getSoldier())
                .log()
                .map(i -> {
                    i.setWeapon("Polearm");
                    return i;
                })
                .subscribeOn(Schedulers.parallel())
                .subscribe(soldiers::add);

        assertThat(soldiers).hasSize(4);
        assertThat(soldiers).allMatch(x -> x.getWeapon().equals("Polearm"));
    }

    private static Soldier getSoldier() {
        var soldier = new Soldier();
        soldier.setId(UUID.randomUUID().toString());
        soldier.setName("Roger");
        soldier.setWeapon("Crossbow");
        return soldier;
    }
}
