package com.nttdata.bankclient.service.impl;

import com.nttdata.bankclient.domain.model.Client;
import com.nttdata.bankclient.domain.model.ClientUpdateData;
import com.nttdata.bankclient.dto.request.CreateClientRequest;
import com.nttdata.bankclient.dto.request.UpdateClientRequest;
import com.nttdata.bankclient.dto.response.ClientResponse;
import com.nttdata.bankclient.exception.ClientNotFoundException;
import com.nttdata.bankclient.exception.DuplicateClientException;
import com.nttdata.bankclient.infraestructure.persistence.mapper.ClientMapper;
import com.nttdata.bankclient.infraestructure.persistence.document.ClientDocument;
import com.nttdata.bankclient.infraestructure.persistence.repository.ClientRepository;
import com.nttdata.bankclient.service.ClientService;
import com.nttdata.bankclient.service.validation.ClientValidationFactory;
import com.nttdata.bankclient.service.validation.ClientValidationStrategy;
import io.reactivex.rxjava3.core.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository repository;
    private final ClientMapper mapper;
    private final ClientValidationFactory clientValidationFactory;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ClientServiceImpl.class);

    @Override
    public Single<ClientResponse> createClient(CreateClientRequest request) {
        return Single.defer(() -> {
            Client client = mapper.toDomain(request);

            LOGGER.info(
                    "Starting client creation. type={}, document={}",
                    client.getClientType(),
                    client.getDocumentNumber()
            );

            clientValidationFactory
                    .getStrategy(client.getClientType()).validate(client);

            return validateDocumentDoesNotExist(client)
                    .andThen(Single.defer(()-> save(client)))
                    .doOnSuccess(response ->
                            LOGGER.info(
                                    "Client created successfully. clientId={}",
                                    response.id()
                            )
                    )
                    .doOnError(error ->
                            LOGGER.error(
                                    "Error creating client. type={}, error={}",
                                    request.clientType(),
                                    error.getMessage()
                            )
                    );
        });
    }

    @Override
    public Maybe<ClientResponse> findById(String id) {
        LOGGER.info("Finding client by id: {}", id);

        return Maybe.fromPublisher(repository.findByIdAndActiveTrue(id))
                .map(x -> mapper.toResponse(x))
                .doOnSuccess(response -> LOGGER.info(
                        "Active client found. clientId={}",
                        response.id()
                ))
                .doOnComplete(() ->
                        LOGGER.warn("Active client was not found. clientId={}", id)
                ).doOnError(error ->
                        LOGGER.error(
                                "Error finding active client. clientId={}, error={}",
                                id,
                                error.getMessage()
                        )
                );
    }

    @Override
    public Maybe<ClientResponse> findByDocumentNumber(
            String documentNumber) {
        String normalizedDocNumber = documentNumber.trim();

        LOGGER.info("Finding client by document number: {}", documentNumber);

        return Maybe
                .fromPublisher(
                        repository.findByDocumentNumberAndActiveTrue(normalizedDocNumber)
                ).map(x -> mapper.toResponse(x))
                .doOnComplete(() ->
                        LOGGER.warn("Active client was not found by document: {}", normalizedDocNumber)
                )
                .doOnError(error ->
                        LOGGER.error(
                                "Server Error finding client by document. error={}",
                                error.getMessage()
                        ));


    }

    @Override
    public Flowable<ClientResponse> findAll() {
        LOGGER.info("Finding all clients");
        return Flowable.fromPublisher(repository.findAllByActiveTrue())
                .map(x -> mapper.toResponse(x))
                .doOnComplete(() ->
                        LOGGER.warn("Finished retrieving all clients")
                )
                .doOnError(error ->
                        LOGGER.error(
                                "Error retrieving clients. error={}",
                                error.getMessage()
                        ));
    }

    @Override
    public Single<ClientResponse> updateClient(String id, UpdateClientRequest request) {
        LOGGER.info("Updating client. clientId={}", id);

        return Maybe
                .fromPublisher(repository.findByIdAndActiveTrue(id))
                .switchIfEmpty(
                        Single.error(
                                new ClientNotFoundException(id)
                        )
                )
                .map(mapper::toDomain)
                .map(client -> applyUpdate(client, request))
                .map(mapper::toDocument)
                .flatMap(document ->
                        Single.fromPublisher(repository.save(document))
                )
                .map(mapper::toResponse)
                .doOnSuccess(response ->
                        LOGGER.info(
                                "Client updated successfully. clientId={}",
                                response.id()
                        )
                )
                .doOnError(error ->
                        LOGGER.error(
                                "Error updating client. clientId={}, error={}",
                                id,
                                error.getMessage()
                        )
                );
    }

    @Override
    public Completable deleteClient(String id) {
        LOGGER.info("Deleting client logically. clientId={}", id);

        return Maybe
                .fromPublisher(repository.findByIdAndActiveTrue(id))
                .switchIfEmpty(
                        Single.error(
                                new ClientNotFoundException(id)
                        )
                )
                .map(mapper::toDomain)
                .map(this::deactivateClient)
                .map(mapper::toDocument)
                .flatMap(document ->
                        Single.fromPublisher(repository.save(document))
                )
                .ignoreElement()
                .doOnComplete(() ->
                        LOGGER.info(
                                "Client deleted logically. clientId={}",
                                id
                        )
                )
                .doOnError(error ->
                        LOGGER.error(
                                "Error deleting client. clientId={}, error={}",
                                id,
                                error.getMessage()
                        )
                );
    }

    private Client deactivateClient(Client client) {
        client.deactivate();
        return client;
    }

    private Client applyUpdate(
            Client client,
            UpdateClientRequest request) {

        ClientUpdateData updateData = new ClientUpdateData(
                normalizeNullable(request.firstName()),
                normalizeNullable(request.lastName()),
                normalizeNullable(request.businessName())
        );

        ClientValidationStrategy strategy =
                clientValidationFactory.getStrategy(
                        client.getClientType()
                );

        strategy.update(client, updateData);

        return client;
    }

    private Single<ClientResponse> save(Client client) {
        ClientDocument clientDocument = mapper.toDocument(client);
        return Single
                .fromPublisher(repository.save(clientDocument))
                .map(x -> mapper.toDomain(x))
                .map(y -> mapper.toResponse(y));
    }

    private Completable validateDocumentDoesNotExist(Client client) {
        return
                Single.fromPublisher(repository.existsByDocumentNumber(client.getDocumentNumber()))
                        .flatMapCompletable(exists -> {
                            if (Boolean.TRUE.equals(exists)) {
                                return Completable.error(
                                        new DuplicateClientException(client.getDocumentNumber()));
                            }

                            return Completable.complete();
                        });
    }

    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }
}
