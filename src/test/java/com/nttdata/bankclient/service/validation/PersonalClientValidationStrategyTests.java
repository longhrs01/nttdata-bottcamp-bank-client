package com.nttdata.bankclient.service.validation;

import com.nttdata.bankclient.domain.enums.ClientType;
import com.nttdata.bankclient.domain.enums.DocumentType;
import com.nttdata.bankclient.domain.model.Client;
import com.nttdata.bankclient.domain.model.ClientUpdateData;
import com.nttdata.bankclient.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonalClientValidationStrategyTests {

    private PersonalClientValidationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PersonalClientValidationStrategy();
    }

    @Test
    void shouldSupportPersonalClientType() {
        assertEquals(
                ClientType.PERSONAL,
                strategy.supportedType()
        );
    }

    @Test
    void shouldValidatePersonalClientSuccessfully() {
        Client client = buildPersonalClient(
                DocumentType.DNI,
                "Chris",
                "Vega",
                null
        );

        strategy.validate(client);
    }

    @Test
    void shouldThrowExceptionWhenPersonalClientUsesInvalidDocumentType() {
        Client client = buildPersonalClient(
                DocumentType.RUC,
                "Chris",
                "Vega",
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> strategy.validate(client)
        );

        assertEquals(
                "Personal clients must use DNI or PASSPORT",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenPersonalClientHasNoFirstName() {
        Client client = buildPersonalClient(
                DocumentType.DNI,
                null,
                "Vega",
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> strategy.validate(client)
        );

        assertEquals(
                "Personal clients must provide first name and last name",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenPersonalClientHasNoLastName() {
        Client client = buildPersonalClient(
                DocumentType.DNI,
                "Chris",
                null,
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> strategy.validate(client)
        );

        assertEquals(
                "Personal clients must provide first name and last name",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenPersonalClientHasBusinessName() {
        Client client = buildPersonalClient(
                DocumentType.DNI,
                "Chris",
                "Vega",
                "Empresa Incorrecta S.A.C."
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> strategy.validate(client)
        );

        assertEquals(
                "Personal clients cannot provide a business name",
                exception.getMessage()
        );
    }

    @Test
    void shouldUpdatePersonalClientSuccessfully() {
        Client client = buildPersonalClient(
                DocumentType.DNI,
                "Chris",
                "Vega",
                null
        );

        ClientUpdateData updateData = new ClientUpdateData(
                "Christopher",
                "Vega Ramos",
                null
        );

        strategy.update(client, updateData);

        assertAll(
                () -> assertEquals(
                        "Christopher",
                        client.getFirstName()
                ),
                () -> assertEquals(
                        "Vega Ramos",
                        client.getLastName()
                ),
                () -> assertEquals(
                        null,
                        client.getBusinessName()
                )
        );
    }

    @Test
    void shouldAllowUpdatingOnlyPersonalFirstName() {
        Client client = buildPersonalClient(
                DocumentType.DNI,
                "Chris",
                "Vega",
                null
        );

        ClientUpdateData updateData = new ClientUpdateData(
                "Christopher",
                null,
                null
        );

        strategy.update(client, updateData);

        assertAll(
                () -> assertEquals(
                        "Christopher",
                        client.getFirstName()
                ),
                () -> assertEquals(
                        "Vega",
                        client.getLastName()
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenPersonalUpdateContainsBusinessName() {
        Client client = buildPersonalClient(
                DocumentType.DNI,
                "Chris",
                "Vega",
                null
        );

        ClientUpdateData updateData = new ClientUpdateData(
                null,
                null,
                "Empresa Incorrecta S.A.C."
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> strategy.update(client, updateData)
        );

        assertEquals(
                "Personal clients cannot update business name",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenPersonalUpdateHasNoAllowedFields() {
        Client client = buildPersonalClient(
                DocumentType.DNI,
                "Chris",
                "Vega",
                null
        );

        ClientUpdateData updateData = new ClientUpdateData(
                null,
                null,
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> strategy.update(client, updateData)
        );

        assertEquals(
                "Personal clients must update first name or last name",
                exception.getMessage()
        );
    }

    private Client buildPersonalClient(
            DocumentType documentType,
            String firstName,
            String lastName,
            String businessName) {

        return new Client(
                "client-001",
                ClientType.PERSONAL,
                documentType,
                "71234567",
                firstName,
                lastName,
                businessName,
                Boolean.TRUE
        );
    }
}