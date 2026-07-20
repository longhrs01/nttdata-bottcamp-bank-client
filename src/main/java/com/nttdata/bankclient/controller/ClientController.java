package com.nttdata.bankclient.controller;

import com.nttdata.bankclient.dto.request.CreateClientRequest;
import com.nttdata.bankclient.dto.request.UpdateClientRequest;
import com.nttdata.bankclient.dto.response.ClientResponse;
import com.nttdata.bankclient.service.ClientService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public Single<ResponseEntity<ClientResponse>> createClient(
            @Valid @RequestBody CreateClientRequest request) {

        return clientService
                .createClient(request)
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }

    @GetMapping
    public Flowable<ClientResponse> findAll() {
        return clientService.findAll();
    }

    @GetMapping("/{id}")
    public Single<ResponseEntity<ClientResponse>> findById(
            @PathVariable String id) {

        return clientService
                .findById(id)
                .map(ResponseEntity::ok)
                .switchIfEmpty(
                        Single.just(
                                ResponseEntity.notFound().build()
                        )
                );
    }

    @GetMapping("/document/{documentNumber}")
    public Single<ResponseEntity<ClientResponse>>
    findByDocumentNumber(
            @PathVariable String documentNumber) {

        return clientService
                .findByDocumentNumber(documentNumber)
                .map(ResponseEntity::ok)
                .switchIfEmpty(
                        Single.just(
                                ResponseEntity.notFound().build()
                        )
                );
    }

    @PutMapping("/{id}")
    public Single<ResponseEntity<ClientResponse>> updateClient(
            @PathVariable String id,
            @Valid @RequestBody UpdateClientRequest request) {

        return clientService
                .updateClient(id, request)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Single<ResponseEntity<Void>> deleteClient(
            @PathVariable String id) {

        return clientService
                .deleteClient(id)
                .andThen(
                        Single.just(
                                ResponseEntity.noContent().build()
                        )
                );
    }
}