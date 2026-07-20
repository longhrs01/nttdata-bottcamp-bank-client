package com.nttdata.bankclient.service.impl;

import com.nttdata.bankclient.domain.enums.ClientType;
import com.nttdata.bankclient.domain.enums.DocumentType;
import com.nttdata.bankclient.domain.model.Client;
import com.nttdata.bankclient.domain.model.ClientUpdateData;
import com.nttdata.bankclient.dto.request.CreateClientRequest;
import com.nttdata.bankclient.dto.request.UpdateClientRequest;
import com.nttdata.bankclient.dto.response.ClientResponse;
import com.nttdata.bankclient.exception.BusinessException;
import com.nttdata.bankclient.exception.ClientNotFoundException;
import com.nttdata.bankclient.exception.DuplicateClientException;
import com.nttdata.bankclient.infraestructure.persistence.mapper.ClientMapper;
import com.nttdata.bankclient.infraestructure.persistence.document.ClientDocument;
import com.nttdata.bankclient.infraestructure.persistence.repository.ClientRepository;
import com.nttdata.bankclient.service.validation.ClientValidationFactory;
import com.nttdata.bankclient.service.validation.ClientValidationStrategy;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTests {

    @Mock
    private ClientRepository repository;

    @Mock
    private ClientMapper mapper;

    @Mock
    private ClientValidationFactory validationFactory;

    @Mock
    private ClientValidationStrategy validationStrategy;

    private ClientServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ClientServiceImpl(
                repository,
                mapper,
                validationFactory
        );
    }

    @Test
    void shouldCreatePersonalClientSuccessfully() {
        CreateClientRequest request = buildPersonalRequest();

        Client client = buildPersonalClient(null);

        ClientDocument documentToSave = buildPersonalDocument(null);

        ClientDocument savedDocument = buildPersonalDocument("client-001");

        Client savedClient = buildPersonalClient("client-001");

        ClientResponse expectedResponse = new ClientResponse(
                "client-001",
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Chris Vega",
                Boolean.TRUE
        );

        when(mapper.toDomain(request))
                .thenReturn(client);

        when(validationFactory.getStrategy(ClientType.PERSONAL))
                .thenReturn(validationStrategy);

        when(repository.existsByDocumentNumber("71234567"))
                .thenReturn(Mono.just(false));

        when(mapper.toDocument(client))
                .thenReturn(documentToSave);

        when(repository.save(documentToSave))
                .thenReturn(Mono.just(savedDocument));

        when(mapper.toDomain(savedDocument))
                .thenReturn(savedClient);

        when(mapper.toResponse(savedClient))
                .thenReturn(expectedResponse);

        TestObserver<ClientResponse> observer = service
                .createClient(request)
                .test();

        observer
                .assertComplete()
                .assertNoErrors()
                .assertValue(expectedResponse);

        verify(mapper).toDomain(request);
        verify(validationFactory)
                .getStrategy(ClientType.PERSONAL);
        verify(validationStrategy).validate(client);
        verify(repository)
                .existsByDocumentNumber("71234567");
        verify(repository).save(documentToSave);
    }

    @Test
    void shouldCreateBusinessClientSuccessfully() {
        CreateClientRequest request = buildBusinessRequest();

        Client client = buildBusinessClient(null);

        ClientDocument documentToSave = buildBusinessDocument(null);

        ClientDocument savedDocument =
                buildBusinessDocument("client-002");

        Client savedClient =
                buildBusinessClient("client-002");

        ClientResponse expectedResponse = new ClientResponse(
                "client-002",
                ClientType.BUSINESS,
                DocumentType.RUC,
                "20123456789",
                "Inversiones Vega S.A.C.",
                Boolean.TRUE
        );

        when(mapper.toDomain(request))
                .thenReturn(client);

        when(validationFactory.getStrategy(ClientType.BUSINESS))
                .thenReturn(validationStrategy);

        when(repository.existsByDocumentNumber("20123456789"))
                .thenReturn(Mono.just(false));

        when(mapper.toDocument(client))
                .thenReturn(documentToSave);

        when(repository.save(documentToSave))
                .thenReturn(Mono.just(savedDocument));

        when(mapper.toDomain(savedDocument))
                .thenReturn(savedClient);

        when(mapper.toResponse(savedClient))
                .thenReturn(expectedResponse);

        service.createClient(request)
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue(expectedResponse);

        verify(validationFactory)
                .getStrategy(ClientType.BUSINESS);
        verify(validationStrategy).validate(client);
        verify(repository)
                .existsByDocumentNumber("20123456789");
        verify(repository).save(documentToSave);
    }

    @Test
    void shouldReturnErrorWhenDocumentAlreadyExists() {
        CreateClientRequest request = buildPersonalRequest();

        Client client = buildPersonalClient(null);

        when(mapper.toDomain(request))
                .thenReturn(client);

        when(validationFactory.getStrategy(ClientType.PERSONAL))
                .thenReturn(validationStrategy);

        when(repository.existsByDocumentNumber("71234567"))
                .thenReturn(Mono.just(true));

        service.createClient(request)
                .test()
                .assertNotComplete()
                .assertError(DuplicateClientException.class);

        verify(validationStrategy).validate(client);
        verify(repository)
                .existsByDocumentNumber("71234567");

        verify(repository, never())
                .save(any(ClientDocument.class));
    }

    @Test
    void shouldNotCheckRepositoryWhenValidationStrategyFails() {
        CreateClientRequest request = buildPersonalRequest();

        Client client = buildPersonalClient(null);

        when(mapper.toDomain(request))
                .thenReturn(client);

        when(validationFactory.getStrategy(ClientType.PERSONAL))
                .thenReturn(validationStrategy);

        doThrow(
                new BusinessException(
                        "Personal clients must use DNI or PASSPORT"
                )
        )
                .when(validationStrategy)
                .validate(client);

        service.createClient(request)
                .test()
                .assertNotComplete()
                .assertError(BusinessException.class);

        verify(validationStrategy).validate(client);

        verifyNoInteractions(repository);
    }

    @Test
    void shouldSaveMappedClientDocument() {
        CreateClientRequest request = buildPersonalRequest();

        Client client = buildPersonalClient(null);

        ClientDocument documentToSave =
                buildPersonalDocument(null);

        ClientDocument savedDocument =
                buildPersonalDocument("client-001");

        Client savedClient =
                buildPersonalClient("client-001");

        ClientResponse response = new ClientResponse(
                "client-001",
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Chris Vega",
                Boolean.TRUE
        );

        when(mapper.toDomain(request))
                .thenReturn(client);

        when(validationFactory.getStrategy(ClientType.PERSONAL))
                .thenReturn(validationStrategy);

        when(repository.existsByDocumentNumber("71234567"))
                .thenReturn(Mono.just(false));

        when(mapper.toDocument(client))
                .thenReturn(documentToSave);

        when(repository.save(any(ClientDocument.class)))
                .thenReturn(Mono.just(savedDocument));

        when(mapper.toDomain(savedDocument))
                .thenReturn(savedClient);

        when(mapper.toResponse(savedClient))
                .thenReturn(response);

        service.createClient(request)
                .test()
                .assertComplete()
                .assertNoErrors();

        ArgumentCaptor<ClientDocument> captor =
                ArgumentCaptor.forClass(ClientDocument.class);

        verify(repository).save(captor.capture());

        ClientDocument captured = captor.getValue();

        assertAll(
                () -> assertEquals(
                        ClientType.PERSONAL,
                        captured.getClientType()
                ),
                () -> assertEquals(
                        DocumentType.DNI,
                        captured.getDocumentType()
                ),
                () -> assertEquals(
                        "71234567",
                        captured.getDocumentNumber()
                ),
                () -> assertEquals(
                        "Chris",
                        captured.getFirstName()
                ),
                () -> assertEquals(
                        "Vega",
                        captured.getLastName()
                ),
                () -> assertTrue(captured.getActive())
        );
    }

    @Test
    void shouldFindClientByIdSuccessfully() {
        String clientId = "client-001";

        ClientDocument document = buildPersonalDocument(clientId);

        ClientResponse expectedResponse = new ClientResponse(
                clientId,
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Chris Vega",
                Boolean.TRUE
        );

        when(repository.findByIdAndActiveTrue(clientId))
                .thenReturn(Mono.just(document));

        when(mapper.toResponse(document))
                .thenReturn(expectedResponse);

        service.findById(clientId)
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue(expectedResponse);

        verify(repository).findByIdAndActiveTrue(clientId);
        verify(mapper).toResponse(document);
    }

    @Test
    void shouldCompleteWithoutValueWhenClientIdDoesNotExist() {
        String clientId = "client-not-found";

        when(repository.findByIdAndActiveTrue(clientId))
                .thenReturn(Mono.empty());

        service.findById(clientId)
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertNoValues();

        verify(repository).findByIdAndActiveTrue(clientId);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldPropagateRepositoryErrorWhenFindingClientById() {
        String clientId = "client-001";

        RuntimeException repositoryException =
                new RuntimeException("MongoDB connection error");

        when(repository.findByIdAndActiveTrue(clientId))
                .thenReturn(Mono.error(repositoryException));

        service.findById(clientId)
                .test()
                .assertNotComplete()
                .assertNoValues()
                .assertError(repositoryException);

        verify(repository).findByIdAndActiveTrue(clientId);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldFindClientByDocumentNumberSuccessfully() {
        String documentNumber = "71234567";

        ClientDocument document = buildPersonalDocument("client-001");

        ClientResponse expectedResponse = new ClientResponse(
                "client-001",
                ClientType.PERSONAL,
                DocumentType.DNI,
                documentNumber,
                "Chris Vega",
                Boolean.TRUE
        );

        when(repository.findByDocumentNumberAndActiveTrue(documentNumber))
                .thenReturn(Mono.just(document));

        when(mapper.toResponse(document))
                .thenReturn(expectedResponse);

        service.findByDocumentNumber(documentNumber)
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue(expectedResponse);

        verify(repository)
                .findByDocumentNumberAndActiveTrue(documentNumber);
    }

    @Test
    void shouldTrimDocumentNumberBeforeSearching() {
        String documentNumberWithSpaces = "  71234567  ";
        String normalizedDocumentNumber = "71234567";

        ClientDocument document = buildPersonalDocument("client-001");

        ClientResponse expectedResponse = new ClientResponse(
                "client-001",
                ClientType.PERSONAL,
                DocumentType.DNI,
                normalizedDocumentNumber,
                "Chris Vega",
                Boolean.TRUE
        );

        when(repository.findByDocumentNumberAndActiveTrue(
                normalizedDocumentNumber
        )).thenReturn(Mono.just(document));

        when(mapper.toResponse(document))
                .thenReturn(expectedResponse);

        service.findByDocumentNumber(documentNumberWithSpaces)
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue(expectedResponse);

        verify(repository)
                .findByDocumentNumberAndActiveTrue(
                        normalizedDocumentNumber
                );
    }

    @Test
    void shouldCompleteWithoutValueWhenDocumentNumberDoesNotExist() {
        String documentNumber = "99999999";

        when(repository.findByDocumentNumberAndActiveTrue(documentNumber))
                .thenReturn(Mono.empty());

        service.findByDocumentNumber(documentNumber)
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertNoValues();

        verify(repository)
                .findByDocumentNumberAndActiveTrue(documentNumber);

        verifyNoInteractions(mapper);
    }

    @Test
    void shouldFindAllActiveClientsSuccessfully() {
        ClientDocument personalDocument =
                buildPersonalDocument("client-001");

        ClientDocument businessDocument =
                buildBusinessDocument("client-002");

        ClientResponse personalResponse = new ClientResponse(
                "client-001",
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Chris Vega",
                Boolean.TRUE
        );

        ClientResponse businessResponse = new ClientResponse(
                "client-002",
                ClientType.BUSINESS,
                DocumentType.RUC,
                "20123456789",
                "Inversiones Vega S.A.C.",
                Boolean.TRUE
        );

        when(repository.findAllByActiveTrue())
                .thenReturn(
                        Flux.just(
                                personalDocument,
                                businessDocument
                        )
                );

        when(mapper.toResponse(personalDocument))
                .thenReturn(personalResponse);

        when(mapper.toResponse(businessDocument))
                .thenReturn(businessResponse);

        service.findAll()
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValues(
                        personalResponse,
                        businessResponse
                );

        verify(repository).findAllByActiveTrue();
        verify(mapper).toResponse(personalDocument);
        verify(mapper).toResponse(businessDocument);
    }

    @Test
    void shouldReturnEmptyFlowableWhenThereAreNoActiveClients() {
        when(repository.findAllByActiveTrue())
                .thenReturn(Flux.empty());

        service.findAll()
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertNoValues();

        verify(repository).findAllByActiveTrue();
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldPropagateRepositoryErrorWhenFindingAllClients() {
        RuntimeException repositoryException =
                new RuntimeException("MongoDB connection error");

        when(repository.findAllByActiveTrue())
                .thenReturn(Flux.error(repositoryException));

        service.findAll()
                .test()
                .assertNotComplete()
                .assertNoValues()
                .assertError(repositoryException);

        verify(repository).findAllByActiveTrue();
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldUpdatePersonalClientSuccessfully() {
        String clientId = "client-001";

        UpdateClientRequest request = new UpdateClientRequest(
                "Christopher",
                "Vega Ramos",
                null
        );

        ClientDocument existingDocument =
                buildPersonalDocument(clientId);

        Client domainClient =
                buildPersonalClient(clientId);

        ClientDocument updatedDocument = new ClientDocument(
                clientId,
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Christopher",
                "Vega Ramos",
                null,
                Boolean.TRUE
        );

        ClientResponse expectedResponse = new ClientResponse(
                clientId,
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Christopher Vega Ramos",
                Boolean.TRUE
        );

        when(repository.findByIdAndActiveTrue(clientId))
                .thenReturn(Mono.just(existingDocument));

        when(mapper.toDomain(existingDocument))
                .thenReturn(domainClient);

        when(validationFactory.getStrategy(ClientType.PERSONAL))
                .thenReturn(validationStrategy);

        when(mapper.toDocument(domainClient))
                .thenReturn(updatedDocument);

        when(repository.save(updatedDocument))
                .thenReturn(Mono.just(updatedDocument));

        when(mapper.toResponse(updatedDocument))
                .thenReturn(expectedResponse);

        service.updateClient(clientId, request)
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue(expectedResponse);

        verify(repository).findByIdAndActiveTrue(clientId);
        verify(validationFactory)
                .getStrategy(ClientType.PERSONAL);

        verify(validationStrategy).update(
                eq(domainClient),
                any(ClientUpdateData.class)
        );

        verify(repository).save(updatedDocument);
    }

    @Test
    void shouldSendNormalizedUpdateDataToPersonalStrategy() {
        String clientId = "client-001";

        UpdateClientRequest request = new UpdateClientRequest(
                " Christopher ",
                " Vega Ramos ",
                null
        );

        ClientDocument existingDocument =
                buildPersonalDocument(clientId);

        Client domainClient =
                buildPersonalClient(clientId);

        ClientDocument updatedDocument =
                buildPersonalDocument(clientId);

        ClientResponse response = new ClientResponse(
                clientId,
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Christopher Vega Ramos",
                Boolean.TRUE
        );

        when(repository.findByIdAndActiveTrue(clientId))
                .thenReturn(Mono.just(existingDocument));

        when(mapper.toDomain(existingDocument))
                .thenReturn(domainClient);

        when(validationFactory.getStrategy(ClientType.PERSONAL))
                .thenReturn(validationStrategy);

        when(mapper.toDocument(domainClient))
                .thenReturn(updatedDocument);

        when(repository.save(updatedDocument))
                .thenReturn(Mono.just(updatedDocument));

        when(mapper.toResponse(updatedDocument))
                .thenReturn(response);

        service.updateClient(clientId, request)
                .test()
                .assertComplete()
                .assertNoErrors();

        ArgumentCaptor<ClientUpdateData> captor =
                ArgumentCaptor.forClass(ClientUpdateData.class);

        verify(validationStrategy).update(
                eq(domainClient),
                captor.capture()
        );

        ClientUpdateData captured = captor.getValue();

        assertAll(
                () -> assertEquals(
                        "Christopher",
                        captured.getFirstName()
                ),
                () -> assertEquals(
                        "Vega Ramos",
                        captured.getLastName()
                ),
                () -> assertNull(
                        captured.getBusinessName()
                )
        );
    }

    @Test
    void shouldUpdateBusinessClientSuccessfully() {
        String clientId = "client-002";

        UpdateClientRequest request = new UpdateClientRequest(
                null,
                null,
                "Inversiones Vega del Norte S.A.C."
        );

        ClientDocument existingDocument =
                buildBusinessDocument(clientId);

        Client domainClient =
                buildBusinessClient(clientId);

        ClientDocument updatedDocument = new ClientDocument(
                clientId,
                ClientType.BUSINESS,
                DocumentType.RUC,
                "20123456789",
                null,
                null,
                "Inversiones Vega del Norte S.A.C.",
                Boolean.TRUE
        );

        ClientResponse expectedResponse = new ClientResponse(
                clientId,
                ClientType.BUSINESS,
                DocumentType.RUC,
                "20123456789",
                "Inversiones Vega del Norte S.A.C.",
                Boolean.TRUE
        );

        when(repository.findByIdAndActiveTrue(clientId))
                .thenReturn(Mono.just(existingDocument));

        when(mapper.toDomain(existingDocument))
                .thenReturn(domainClient);

        when(validationFactory.getStrategy(ClientType.BUSINESS))
                .thenReturn(validationStrategy);

        when(mapper.toDocument(domainClient))
                .thenReturn(updatedDocument);

        when(repository.save(updatedDocument))
                .thenReturn(Mono.just(updatedDocument));

        when(mapper.toResponse(updatedDocument))
                .thenReturn(expectedResponse);

        service.updateClient(clientId, request)
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue(expectedResponse);

        verify(validationStrategy).update(
                eq(domainClient),
                any(ClientUpdateData.class)
        );

        verify(repository).save(updatedDocument);
    }

    @Test
    void shouldReturnErrorWhenUpdatingNonExistingClient() {
        String clientId = "client-not-found";

        UpdateClientRequest request = new UpdateClientRequest(
                "Christopher",
                null,
                null
        );

        when(repository.findByIdAndActiveTrue(clientId))
                .thenReturn(Mono.empty());

        service.updateClient(clientId, request)
                .test()
                .assertNotComplete()
                .assertNoValues()
                .assertError(ClientNotFoundException.class);

        verify(repository).findByIdAndActiveTrue(clientId);

        verifyNoInteractions(
                validationFactory,
                validationStrategy,
                mapper
        );

        verify(repository, never())
                .save(any(ClientDocument.class));
    }

    @Test
    void shouldNotSaveWhenUpdateStrategyFails() {
        String clientId = "client-001";

        UpdateClientRequest request = new UpdateClientRequest(
                null,
                null,
                "Invalid Business Name"
        );

        ClientDocument existingDocument =
                buildPersonalDocument(clientId);

        Client domainClient =
                buildPersonalClient(clientId);

        when(repository.findByIdAndActiveTrue(clientId))
                .thenReturn(Mono.just(existingDocument));

        when(mapper.toDomain(existingDocument))
                .thenReturn(domainClient);

        when(validationFactory.getStrategy(ClientType.PERSONAL))
                .thenReturn(validationStrategy);

        doThrow(
                new BusinessException(
                        "Personal clients cannot update business name"
                )
        ).when(validationStrategy)
                .update(
                        eq(domainClient),
                        any(ClientUpdateData.class)
                );

        service.updateClient(clientId, request)
                .test()
                .assertNotComplete()
                .assertNoValues()
                .assertError(BusinessException.class);

        verify(repository, never())
                .save(any(ClientDocument.class));
    }

    @Test
    void shouldDeleteClientLogicallySuccessfully() {
        String clientId = "client-001";

        ClientDocument existingDocument =
                buildPersonalDocument(clientId);

        Client domainClient =
                buildPersonalClient(clientId);

        ClientDocument inactiveDocument = new ClientDocument(
                clientId,
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Chris",
                "Vega",
                null,
                Boolean.FALSE
        );

        when(repository.findByIdAndActiveTrue(clientId))
                .thenReturn(Mono.just(existingDocument));

        when(mapper.toDomain(existingDocument))
                .thenReturn(domainClient);

        when(mapper.toDocument(domainClient))
                .thenReturn(inactiveDocument);

        when(repository.save(inactiveDocument))
                .thenReturn(Mono.just(inactiveDocument));

        service.deleteClient(clientId)
                .test()
                .assertComplete()
                .assertNoErrors();

        assertFalse(domainClient.getActive());

        verify(repository).findByIdAndActiveTrue(clientId);
        verify(repository).save(inactiveDocument);
    }

    @Test
    void shouldSaveInactiveDocumentWhenDeletingClient() {
        String clientId = "client-001";

        ClientDocument existingDocument =
                buildPersonalDocument(clientId);

        Client domainClient =
                buildPersonalClient(clientId);

        when(repository.findByIdAndActiveTrue(clientId))
                .thenReturn(Mono.just(existingDocument));

        when(mapper.toDomain(existingDocument))
                .thenReturn(domainClient);

        when(mapper.toDocument(domainClient))
                .thenAnswer(invocation -> {
                    Client client = invocation.getArgument(0);

                    return new ClientDocument(
                            client.getId(),
                            client.getClientType(),
                            client.getDocumentType(),
                            client.getDocumentNumber(),
                            client.getFirstName(),
                            client.getLastName(),
                            client.getBusinessName(),
                            client.getActive()
                    );
                });

        when(repository.save(any(ClientDocument.class)))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0))
                );

        service.deleteClient(clientId)
                .test()
                .assertComplete()
                .assertNoErrors();

        ArgumentCaptor<ClientDocument> captor =
                ArgumentCaptor.forClass(ClientDocument.class);

        verify(repository).save(captor.capture());

        ClientDocument savedDocument = captor.getValue();

        assertAll(
                () -> assertEquals(
                        clientId,
                        savedDocument.getId()
                ),
                () -> assertFalse(
                        savedDocument.getActive()
                )
        );
    }

    @Test
    void shouldReturnErrorWhenDeletingNonExistingClient() {
        String clientId = "client-not-found";

        when(repository.findByIdAndActiveTrue(clientId))
                .thenReturn(Mono.empty());

        service.deleteClient(clientId)
                .test()
                .assertNotComplete()
                .assertError(ClientNotFoundException.class);

        verify(repository).findByIdAndActiveTrue(clientId);
        verifyNoInteractions(mapper);

        verify(repository, never())
                .save(any(ClientDocument.class));
    }

    @Test
    void shouldPropagateRepositoryErrorWhenDeletingClient() {
        String clientId = "client-001";

        RuntimeException repositoryException =
                new RuntimeException("MongoDB connection error");

        when(repository.findByIdAndActiveTrue(clientId))
                .thenReturn(Mono.error(repositoryException));

        service.deleteClient(clientId)
                .test()
                .assertNotComplete()
                .assertError(repositoryException);

        verify(repository).findByIdAndActiveTrue(clientId);
        verifyNoInteractions(mapper);
    }

    private CreateClientRequest buildPersonalRequest() {
        return new CreateClientRequest(
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Chris",
                "Vega",
                null
        );
    }

    private CreateClientRequest buildBusinessRequest() {
        return new CreateClientRequest(
                ClientType.BUSINESS,
                DocumentType.RUC,
                "20123456789",
                null,
                null,
                "Inversiones Vega S.A.C."
        );
    }

    private Client buildPersonalClient(String id) {
        return new Client(
                id,
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Chris",
                "Vega",
                null,
                Boolean.TRUE
        );
    }

    private Client buildBusinessClient(String id) {
        return new Client(
                id,
                ClientType.BUSINESS,
                DocumentType.RUC,
                "20123456789",
                null,
                null,
                "Inversiones Vega S.A.C.",
                Boolean.TRUE
        );
    }

    private ClientDocument buildPersonalDocument(String id) {
        return new ClientDocument(
                id,
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Chris",
                "Vega",
                null,
                Boolean.TRUE
        );
    }

    private ClientDocument buildBusinessDocument(String id) {
        return new ClientDocument(
                id,
                ClientType.BUSINESS,
                DocumentType.RUC,
                "20123456789",
                null,
                null,
                "Inversiones Vega S.A.C.",
                Boolean.TRUE
        );
    }
}