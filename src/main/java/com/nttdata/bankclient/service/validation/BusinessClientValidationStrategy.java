package com.nttdata.bankclient.service.validation;

import com.nttdata.bankclient.domain.enums.ClientType;
import com.nttdata.bankclient.domain.enums.DocumentType;
import com.nttdata.bankclient.domain.model.Client;
import com.nttdata.bankclient.domain.model.ClientUpdateData;
import com.nttdata.bankclient.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class BusinessClientValidationStrategy implements ClientValidationStrategy {
    @Override
    public ClientType supportedType() {
        return ClientType.BUSINESS;
    }

    @Override
    public void validate(Client client) {
        validateDocumentType(client);
        validateBusinessInformation(client);
    }

    @Override
    public void update(Client client, ClientUpdateData updateData) {
        validateUpdateInformation(updateData);

        client.updateBusinessInformation(
                updateData.getBusinessName()
        );
    }

    private void validateUpdateInformation(
            ClientUpdateData updateData) {

        if (hasText(updateData.getFirstName())
                || hasText(updateData.getLastName())) {

            throw new BusinessException(
                    "Business clients cannot update first name or last name"
            );
        }

        if (!hasText(updateData.getBusinessName())) {
            throw new BusinessException(
                    "Business clients must update business name"
            );
        }
    }

    private void validateDocumentType(Client client) {
        if (!DocumentType.RUC.equals(client.getDocumentType())) {
            throw new BusinessException(
                    "Business clients must use RUC"
            );
        }
    }

    private void validateBusinessInformation(Client client) {
        if (!hasText(client.getBusinessName())) {
            throw new BusinessException(
                    "Business clients must provide a business name"
            );
        }

        if (hasText(client.getFirstName())
                || hasText(client.getLastName())) {

            throw new BusinessException(
                    "Business clients cannot provide first name or last name"
            );
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
