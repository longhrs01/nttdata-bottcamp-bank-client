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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessClientValidationStrategyTests {

    private BusinessClientValidationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BusinessClientValidationStrategy();
    }

    @Test
    void shouldSupportBusinessClientType() {
        assertEquals(
                ClientType.BUSINESS,
                strategy.supportedType()
        );
    }

    @Test
    void shouldValidateBusinessClientSuccessfully() {
        Client client = buildBusinessClient(
                DocumentType.RUC,
                null,
                null,
                "Inversiones Vega S.A.C."
        );

        strategy.validate(client);
    }

    @Test
    void shouldThrowExceptionWhenBusinessClientUsesInvalidDocumentType() {
        Client client = buildBusinessClient(
                DocumentType.DNI,
                null,
                null,
                "Inversiones Vega S.A.C."
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> strategy.validate(client)
        );

        assertEquals(
                "Business clients must use RUC",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenBusinessClientHasNoBusinessName() {
        Client client = buildBusinessClient(
                DocumentType.RUC,
                null,
                null,
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> strategy.validate(client)
        );

        assertEquals(
                "Business clients must provide a business name",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenBusinessClientHasFirstName() {
        Client client = buildBusinessClient(
                DocumentType.RUC,
                "Chris",
                null,
                "Inversiones Vega S.A.C."
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> strategy.validate(client)
        );

        assertEquals(
                "Business clients cannot provide first name or last name",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenBusinessClientHasLastName() {
        Client client = buildBusinessClient(
                DocumentType.RUC,
                null,
                "Vega",
                "Inversiones Vega S.A.C."
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> strategy.validate(client)
        );

        assertEquals(
                "Business clients cannot provide first name or last name",
                exception.getMessage()
        );
    }

    @Test
    void shouldUpdateBusinessClientSuccessfully() {
        Client client = buildBusinessClient(
                DocumentType.RUC,
                null,
                null,
                "Inversiones Vega S.A.C."
        );

        ClientUpdateData updateData = new ClientUpdateData(
                null,
                null,
                "Inversiones Vega del Norte S.A.C."
        );

        strategy.update(client, updateData);

        assertAll(
                () -> assertEquals(
                        "Inversiones Vega del Norte S.A.C.",
                        client.getBusinessName()
                ),
                () -> assertNull(client.getFirstName()),
                () -> assertNull(client.getLastName())
        );
    }

    @Test
    void shouldThrowExceptionWhenBusinessUpdateContainsFirstName() {
        Client client = buildBusinessClient(
                DocumentType.RUC,
                null,
                null,
                "Inversiones Vega S.A.C."
        );

        ClientUpdateData updateData = new ClientUpdateData(
                "Chris",
                null,
                "Nueva Empresa S.A.C."
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> strategy.update(client, updateData)
        );

        assertEquals(
                "Business clients cannot update first name or last name",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenBusinessUpdateContainsLastName() {
        Client client = buildBusinessClient(
                DocumentType.RUC,
                null,
                null,
                "Inversiones Vega S.A.C."
        );

        ClientUpdateData updateData = new ClientUpdateData(
                null,
                "Vega",
                "Nueva Empresa S.A.C."
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> strategy.update(client, updateData)
        );

        assertEquals(
                "Business clients cannot update first name or last name",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenBusinessUpdateHasNoBusinessName() {
        Client client = buildBusinessClient(
                DocumentType.RUC,
                null,
                null,
                "Inversiones Vega S.A.C."
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
                "Business clients must update business name",
                exception.getMessage()
        );
    }

    private Client buildBusinessClient(
            DocumentType documentType,
            String firstName,
            String lastName,
            String businessName) {

        return new Client(
                "client-002",
                ClientType.BUSINESS,
                documentType,
                "20123456789",
                firstName,
                lastName,
                businessName,
                Boolean.TRUE
        );
    }
}