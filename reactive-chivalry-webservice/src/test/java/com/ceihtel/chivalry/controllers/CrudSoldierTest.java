package com.ceihtel.chivalry.controllers;

import com.ceihtel.chivalry.entities.Soldier;
import com.ceihtel.chivalry.mappers.SoldierMapper;
import com.ceihtel.chivalry.mappers.SoldierMapperImpl;
import com.ceihtel.chivalry.repositories.SoldierRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.*;

@WebFluxTest(SoldierController.class)
@Slf4j
class CrudSoldierTest {
    @TestConfiguration
    public static class CrudSoldierTestConfiguration {
        @Bean
        public SoldierMapper soldierMapper() {
            return new SoldierMapperImpl();
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SoldierRepository soldierRepository;

    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(serverResponse -> {
            log.info("Response: {}", serverResponse.statusCode());
            return Mono.just(serverResponse);
        });
    }

    @BeforeEach
    public void setup() {
        webTestClient = webTestClient.mutate().filter(logRequest()).filter(logResponse()).build();
        Mockito.reset(soldierRepository);
    }

    private static Soldier getSoldier(String name, String weapon) {
        var soldier = new Soldier();
        soldier.setId(UUID.randomUUID().toString());
        soldier.setName(name);
        soldier.setWeapon(weapon);
        return soldier;
    }

    @Nested
    @DisplayName("Get all")
    @Import(CrudSoldierTestConfiguration.class)
    class GetAllTests {

        @Test
        void shouldGetAll() {
            when(soldierRepository.findAll()).thenReturn(
                    Flux.just(getSoldier("Roger", "Crossbow"), getSoldier("Tancrède", "Polearm")));

            webTestClient
                    .get().uri("/soldiers")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$").isArray()
                    .jsonPath("$[0].name").isEqualTo("Roger")
                    .jsonPath("$[0].weapon").isEqualTo("Crossbow")
                    .jsonPath("$[1].name").isEqualTo("Tancrède")
                    .jsonPath("$[1].weapon").isEqualTo("Polearm");

            verify(soldierRepository).findAll();
            verifyNoMoreInteractions(soldierRepository);
        }

        @Test
        void shouldReturnEmptyList() {
            webTestClient
                    .get().uri("/soldiers")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBodyList(Soldier.class)
                    .hasSize(0);

            verify(soldierRepository).findAll();
            verifyNoMoreInteractions(soldierRepository);
        }
    }

    @Nested
    @DisplayName("Get one")
    @Import(CrudSoldierTestConfiguration.class)
    class GetOneTests {

        @Test
        void shouldReturnOneSoldier() {
            when(soldierRepository.findById("1234")).thenReturn(
                    Mono.just(getSoldier("Roger", "Crossbow")));

            webTestClient
                    .get().uri("/soldiers/1234")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.name").isEqualTo("Roger")
                    .jsonPath("$.weapon").isEqualTo("Crossbow");

            verify(soldierRepository).findById("1234");
            verifyNoMoreInteractions(soldierRepository);
        }

        @Test
        void shouldFailWhenMissing() {
            when(soldierRepository.findById("1234")).thenReturn(Mono.empty());

            webTestClient
                    .get().uri("/soldiers/1234")
                    .exchange()
                    .expectStatus()
                    .isNotFound()
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Could not find Soldier with id '1234'");

            verify(soldierRepository).findById("1234");
            verifyNoMoreInteractions(soldierRepository);
        }
    }
}
