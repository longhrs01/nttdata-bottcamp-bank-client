package com.nttdata.bankclient.service.validation;

import com.nttdata.bankclient.domain.enums.ClientType;
import com.nttdata.bankclient.domain.enums.DocumentType;
import com.nttdata.bankclient.domain.model.Client;
import com.nttdata.bankclient.domain.model.ClientUpdateData;
import com.nttdata.bankclient.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class PersonalClientValidationStrategy
        implements ClientValidationStrategy {
    private static final Set<DocumentType> ALLOWED_DOCUMENT_TYPES =
            EnumSet.of(
                    DocumentType.DNI,
                    DocumentType.PASSPORT
            );

    @Override
    public ClientType supportedType() {
        return ClientType.PERSONAL;
    }

    @Override
    public void validate(Client client) {
        validateDocumentType(client);
        validatePersonInformation(client);
    }

    @Override
    public void update(
            Client client,
            ClientUpdateData updateData) {

        validateUpdateInformation(updateData);

        client.updatePersonalInformation(
                updateData.getFirstName(),
                updateData.getLastName()
        );
    }

    private void validateDocumentType(Client client) {
        if (!ALLOWED_DOCUMENT_TYPES.contains(client.getDocumentType())) {
            throw new BusinessException("Personal clients must use DNI or PASSPORT");
        }
    }

    private void validatePersonInformation(Client client) {
        if (!hasText(client.getFirstName())
                || !hasText(client.getLastName())) {

            throw new BusinessException(
                    "Personal clients must provide first name and last name"
            );
        }

        if (hasText(client.getBusinessName())) {
            throw new BusinessException(
                    "Personal clients cannot provide a business name"
            );
        }
    }

    private void validateUpdateInformation(
            ClientUpdateData updateData) {

        if (hasText(updateData.getBusinessName())) {
            throw new BusinessException(
                    "Personal clients cannot update business name"
            );
        }

        if (!hasText(updateData.getFirstName())
                && !hasText(updateData.getLastName())) {

            throw new BusinessException(
                    "Personal clients must update first name or last name"
            );
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
