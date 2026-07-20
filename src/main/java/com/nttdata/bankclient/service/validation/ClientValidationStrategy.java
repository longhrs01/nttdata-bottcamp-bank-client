package com.nttdata.bankclient.service.validation;

import com.nttdata.bankclient.domain.enums.ClientType;
import com.nttdata.bankclient.domain.model.Client;
import com.nttdata.bankclient.domain.model.ClientUpdateData;
import com.nttdata.bankclient.dto.request.UpdateClientRequest;

public interface ClientValidationStrategy {
    ClientType supportedType();
    void validate(Client client);
    void update(Client client, ClientUpdateData request);
}
