package com.ceihtel.chivalry.controllers;

import com.ceihtel.chivalry.entities.Soldier;
import com.ceihtel.chivalry.mappers.SoldierMapper;
import com.ceihtel.chivalry.mappers.SoldierMapperImpl;
import com.ceihtel.chivalry.repositories.SoldierRepository;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.mockito.hamcrest.MockitoHamcrest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@WebFluxTest(SoldierController.class)
@Slf4j
public class CrudSoldierTest {
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
    }

    @AfterEach
    public void tearDown() {
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
            Mockito.when(soldierRepository.findAll()).thenReturn(
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

            Mockito.verify(soldierRepository).findAll();
            Mockito.verifyNoMoreInteractions(soldierRepository);
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

            Mockito.verify(soldierRepository).findAll();
            Mockito.verifyNoMoreInteractions(soldierRepository);
        }
    }

    @Nested
    @DisplayName("Get one")
    @Import(CrudSoldierTestConfiguration.class)
    class GetOneTests {

        @Test
        void shouldReturnOneSoldier() {
            Mockito.when(soldierRepository.findByName("Roger")).thenReturn(
                    Mono.just(getSoldier("Roger", "Crossbow")));

            webTestClient
                    .get().uri("/soldiers/Roger")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$.name").isEqualTo("Roger")
                    .jsonPath("$.weapon").isEqualTo("Crossbow");

            Mockito.verify(soldierRepository).findByName("Roger");
            Mockito.verifyNoMoreInteractions(soldierRepository);
        }

        @Test
        void shouldFailWhenMissing() {
            Mockito.when(soldierRepository.findByName("Roger")).thenReturn(Mono.empty());

            webTestClient
                    .get().uri("/soldiers/Roger")
                    .exchange()
                    .expectStatus()
                    .isNotFound()
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Could not find a soldier called 'Roger'");

            Mockito.verify(soldierRepository).findByName("Roger");
            Mockito.verifyNoMoreInteractions(soldierRepository);
        }
    }

    @Nested
    @DisplayName("Create")
    @Import(CrudSoldierTestConfiguration.class)
    class CreateTests {

        @Test
        void shouldCreateSoldier() {
            Mockito.when(soldierRepository.findByName("Roger")).thenReturn(Mono.empty());
            Mockito.when(soldierRepository.save(any(Soldier.class))).thenReturn(Mono.just(getSoldier("Roger", "Shortsword")));

            webTestClient
                    .post().uri("/soldiers")
                    .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue("{ \"name\": \"Roger\", \"weapon\": \"Shortsword\" }"))
                    .exchange()
                    .expectStatus()
                    .isCreated()
                    .expectBody()
                    .jsonPath("$.name").isEqualTo("Roger")
                    .jsonPath("$.weapon").isEqualTo("Shortsword");

            Mockito.verify(soldierRepository).findByName("Roger");
            Mockito.verify(soldierRepository).save(MockitoHamcrest.argThat(allOf(
                    Matchers.isA(Soldier.class),
                    Matchers.<Soldier>hasProperty("name", is("Roger")),
                    Matchers.<Soldier>hasProperty("weapon", is("Shortsword")))));
            Mockito.verifyNoMoreInteractions(soldierRepository);
        }

        @Test
        void shouldFail_missingParameters() {
            webTestClient
                    .post().uri("/soldiers")
                    .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue("{ \"name\": \"\", \"weapon\": \"\" }"))
                    .exchange()
                    .expectStatus()
                    .isBadRequest();

            Mockito.verifyNoInteractions(soldierRepository);
        }

        @Test
        void shouldFail_alreadyExist() {
            Mockito.when(soldierRepository.findByName("Roger")).thenReturn(Mono.just(getSoldier("Roger", "Crossbow")));

            webTestClient
                    .post().uri("/soldiers")
                    .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue("{ \"name\": \"Roger\", \"weapon\": \"Crossbow\" }"))
                    .exchange()
                    .expectStatus()
                    .isEqualTo(HttpStatus.CONFLICT)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("A soldier called 'Roger' already exists");

            Mockito.verify(soldierRepository).findByName("Roger");
            Mockito.verifyNoMoreInteractions(soldierRepository);
        }

        @Test
        void shouldCreateRandomSoldier() {
            Mockito.when(soldierRepository.save(any(Soldier.class))).thenReturn(Mono.just(getSoldier("Roger", "Shortsword")));

            webTestClient
                    .post().uri("/soldiers/random")
                    .exchange()
                    .expectStatus()
                    .isCreated();

            Mockito.verify(soldierRepository).save(any(Soldier.class));
            Mockito.verifyNoMoreInteractions(soldierRepository);
        }
    }

    @Nested
    @DisplayName("Delete")
    @Import(CrudSoldierTestConfiguration.class)
    class DeleteTests {

        @Test
        void shouldDeleteSoldier() {
            Mockito.when(soldierRepository.findByName("Roger")).thenReturn(Mono.just(getSoldier("Roger", "Crossbow")));
            Mockito.when(soldierRepository.delete(any(Soldier.class))).thenReturn(Mono.empty());

            webTestClient
                    .delete().uri("/soldiers/Roger")
                    .exchange()
                    .expectStatus()
                    .isNoContent();

            Mockito.verify(soldierRepository).findByName("Roger");
            Mockito.verify(soldierRepository).delete(MockitoHamcrest.argThat(allOf(
                    Matchers.isA(Soldier.class),
                    Matchers.<Soldier>hasProperty("name", is("Roger")),
                    Matchers.<Soldier>hasProperty("weapon", is("Crossbow")))));
            Mockito.verifyNoMoreInteractions(soldierRepository);
        }

        @Test
        void failure_missingSoldier() {
            Mockito.when(soldierRepository.findByName("Roger")).thenReturn(Mono.empty());

            webTestClient
                    .delete().uri("/soldiers/Roger")
                    .exchange()
                    .expectStatus()
                    .isNotFound()
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Could not find a soldier called 'Roger'");

            Mockito.verify(soldierRepository).findByName("Roger");
            Mockito.verifyNoMoreInteractions(soldierRepository);
        }

        @Test
        void shouldDeleteAll() {
            Mockito.when(soldierRepository.findAll()).thenReturn(Flux.just(
                    getSoldier("Roger", "Crossbow"),
                    getSoldier("Géraud", "Polearm")));
            Mockito.when(soldierRepository.delete(any(Soldier.class))).thenReturn(Mono.empty());

            webTestClient
                    .delete().uri("/soldiers")
                    .exchange()
                    .expectStatus()
                    .isNoContent();

            Mockito.verify(soldierRepository).findAll();
            Mockito.verify(soldierRepository).delete(MockitoHamcrest.argThat(allOf(
                    Matchers.isA(Soldier.class),
                    Matchers.<Soldier>hasProperty("name", is("Roger")),
                    Matchers.<Soldier>hasProperty("weapon", is("Crossbow")))));
            Mockito.verify(soldierRepository).delete(MockitoHamcrest.argThat(allOf(
                    Matchers.isA(Soldier.class),
                    Matchers.<Soldier>hasProperty("name", is("Géraud")),
                    Matchers.<Soldier>hasProperty("weapon", is("Polearm")))));
            Mockito.verifyNoMoreInteractions(soldierRepository);
        }
    }
}
