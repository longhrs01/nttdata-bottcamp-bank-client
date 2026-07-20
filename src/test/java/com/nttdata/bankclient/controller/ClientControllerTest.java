package com.nttdata.bankclient.controller;

import com.nttdata.bankclient.domain.enums.ClientType;
import com.nttdata.bankclient.domain.enums.DocumentType;
import com.nttdata.bankclient.dto.request.CreateClientRequest;
import com.nttdata.bankclient.dto.request.UpdateClientRequest;
import com.nttdata.bankclient.dto.response.ClientResponse;
import com.nttdata.bankclient.exception.GlobalExceptionHandler;
import com.nttdata.bankclient.service.ClientService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        ClientController controller =
                new ClientController(clientService);

        webTestClient = WebTestClient
                .bindToController(controller)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreatePersonalClientAndReturnCreated() {
        CreateClientRequest request = new CreateClientRequest(
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Chris",
                "Vega",
                null
        );

        ClientResponse response = buildPersonalResponse();

        when(clientService.createClient(request))
                .thenReturn(Single.just(response));

        webTestClient.post()
                .uri("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("client-001")
                .jsonPath("$.clientType").isEqualTo("PERSONAL")
                .jsonPath("$.documentType").isEqualTo("DNI")
                .jsonPath("$.documentNumber").isEqualTo("71234567")
                .jsonPath("$.displayName").isEqualTo("Chris Vega")
                .jsonPath("$.active").isEqualTo(true);

        verify(clientService).createClient(request);
    }

    @Test
    void shouldReturnBadRequestWhenPersonalClientHasNoFirstName() {
        CreateClientRequest request = new CreateClientRequest(
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                null,
                "Vega",
                null
        );

        webTestClient.post()
                .uri("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION_ERROR");

        verify(clientService, org.mockito.Mockito.never())
                .createClient(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldReturnAllActiveClients() {
        ClientResponse personal = buildPersonalResponse();

        ClientResponse business = new ClientResponse(
                "client-002",
                ClientType.BUSINESS,
                DocumentType.RUC,
                "20123456789",
                "Inversiones Vega S.A.C.",
                Boolean.TRUE
        );

        when(clientService.findAll())
                .thenReturn(Flowable.just(personal, business));

        webTestClient.get()
                .uri("/api/v1/clients")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("client-001")
                .jsonPath("$[0].clientType").isEqualTo("PERSONAL")
                .jsonPath("$[1].id").isEqualTo("client-002")
                .jsonPath("$[1].clientType").isEqualTo("BUSINESS");

        verify(clientService).findAll();
    }

    @Test
    void shouldReturnEmptyArrayWhenThereAreNoClients() {
        when(clientService.findAll())
                .thenReturn(Flowable.empty());

        webTestClient.get()
                .uri("/api/v1/clients")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("[]");

        verify(clientService).findAll();
    }
    @Test
    void shouldFindClientById() {
        ClientResponse response = buildPersonalResponse();

        when(clientService.findById("client-001"))
                .thenReturn(Maybe.just(response));

        webTestClient.get()
                .uri("/api/v1/clients/client-001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("client-001")
                .jsonPath("$.displayName").isEqualTo("Chris Vega");

        verify(clientService).findById("client-001");
    }

    @Test
    void shouldReturnNotFoundWhenClientIdDoesNotExist() {
        when(clientService.findById("client-not-found"))
                .thenReturn(Maybe.empty());

        webTestClient.get()
                .uri("/api/v1/clients/client-not-found")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();

        verify(clientService).findById("client-not-found");
    }

    @Test
    void shouldFindClientByDocumentNumber() {
        ClientResponse response = buildPersonalResponse();

        when(clientService.findByDocumentNumber("71234567"))
                .thenReturn(Maybe.just(response));

        webTestClient.get()
                .uri("/api/v1/clients/document/71234567")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("client-001")
                .jsonPath("$.documentNumber").isEqualTo("71234567");

        verify(clientService)
                .findByDocumentNumber("71234567");
    }

    @Test
    void shouldReturnNotFoundWhenDocumentDoesNotExist() {
        when(clientService.findByDocumentNumber("99999999"))
                .thenReturn(Maybe.empty());

        webTestClient.get()
                .uri("/api/v1/clients/document/99999999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();

        verify(clientService)
                .findByDocumentNumber("99999999");
    }

    @Test
    void shouldUpdatePersonalClient() {
        UpdateClientRequest request = new UpdateClientRequest(
                "Christopher",
                "Vega Ramos",
                null
        );

        ClientResponse response = new ClientResponse(
                "client-001",
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Christopher Vega Ramos",
                Boolean.TRUE
        );

        when(clientService.updateClient("client-001", request))
                .thenReturn(Single.just(response));

        webTestClient.put()
                .uri("/api/v1/clients/client-001")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("client-001")
                .jsonPath("$.displayName")
                .isEqualTo("Christopher Vega Ramos");

        verify(clientService)
                .updateClient("client-001", request);
    }

    @Test
    void shouldReturnBadRequestWhenUpdateRequestIsEmpty() {
        UpdateClientRequest request =
                new UpdateClientRequest(null, null, null);

        webTestClient.put()
                .uri("/api/v1/clients/client-001")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION_ERROR");

        verify(clientService, org.mockito.Mockito.never())
                .updateClient(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any()
                );
    }
    @Test
    void shouldDeleteClientAndReturnNoContent() {
        when(clientService.deleteClient("client-001"))
                .thenReturn(Completable.complete());

        webTestClient.delete()
                .uri("/api/v1/clients/client-001")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(clientService).deleteClient("client-001");
    }

    private ClientResponse buildPersonalResponse() {
        return new ClientResponse(
                "client-001",
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Chris Vega",
                Boolean.TRUE
        );
    }
}