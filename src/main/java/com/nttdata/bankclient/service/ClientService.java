package com.nttdata.bankclient.service;

import com.nttdata.bankclient.dto.request.CreateClientRequest;
import com.nttdata.bankclient.dto.request.UpdateClientRequest;
import com.nttdata.bankclient.dto.response.ClientResponse;
import io.reactivex.rxjava3.core.*;

public interface ClientService {
    Single<ClientResponse> createClient(CreateClientRequest request);
    Maybe<ClientResponse> findById(String id);
    Maybe<ClientResponse> findByDocumentNumber(String documentNumber);
    Flowable<ClientResponse> findAll();
    Single<ClientResponse> updateClient(
            String id,
            UpdateClientRequest request
    );
    Completable deleteClient(String id);
}
