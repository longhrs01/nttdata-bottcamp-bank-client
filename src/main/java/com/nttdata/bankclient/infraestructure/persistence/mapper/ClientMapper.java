package com.nttdata.bankclient.infraestructure.persistence.mapper;

import com.nttdata.bankclient.domain.enums.ClientType;
import com.nttdata.bankclient.domain.model.Client;
import com.nttdata.bankclient.dto.request.CreateClientRequest;
import com.nttdata.bankclient.dto.response.ClientResponse;
import com.nttdata.bankclient.infraestructure.persistence.document.ClientDocument;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {
    public Client toDomain(CreateClientRequest request) {
        return new Client(null,
                request.clientType(),
                request.documentType(),
                request.documentNumber(),
                request.firstName(),
                request.lastName(),
                request.businessName(),
                Boolean.TRUE
        );
    }

    public Client toDomain(ClientDocument document) {
        return new Client(
                document.getId(),
                document.getClientType(),
                document.getDocumentType(),
                document.getDocumentNumber(),
                document.getFirstName(),
                document.getLastName(),
                document.getBusinessName(),
                document.getActive()
        );
    }

    public ClientDocument toDocument(Client client) {
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
    }

    public ClientResponse toResponse(ClientDocument document) {
        return toResponse(toDomain(document));
    }

    public ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getClientType(),
                client.getDocumentType(),
                client.getDocumentNumber(),
                buildDisplayName(client),
                client.getActive()
        );
    }


    private String buildDisplayName(Client client) {
        if (ClientType.BUSINESS.equals(client.getClientType())) {
            return client.getBusinessName();
        }

        return String.join(
                " ",
                nullToEmpty(client.getFirstName()),
                nullToEmpty(client.getLastName())
        ).trim();
    }
    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
