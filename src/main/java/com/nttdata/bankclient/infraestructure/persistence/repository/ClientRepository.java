package com.nttdata.bankclient.infraestructure.persistence.repository;

import com.nttdata.bankclient.infraestructure.persistence.document.ClientDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClientRepository extends ReactiveMongoRepository<ClientDocument, String> {
    Mono<Boolean> existsByDocumentNumber(String documentNumber);
    Mono<ClientDocument> findByDocumentNumber(String documentNumber);
    Mono<ClientDocument> findByIdAndActiveTrue(String id);
    Mono<ClientDocument> findByDocumentNumberAndActiveTrue(String documentNumber);
    Flux<ClientDocument> findAllByActiveTrue();
}
