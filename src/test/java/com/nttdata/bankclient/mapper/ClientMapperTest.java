package com.nttdata.bankclient.mapper;

import com.nttdata.bankclient.domain.enums.ClientType;
import com.nttdata.bankclient.domain.enums.DocumentType;
import com.nttdata.bankclient.domain.model.Client;
import com.nttdata.bankclient.dto.request.CreateClientRequest;
import com.nttdata.bankclient.dto.response.ClientResponse;
import com.nttdata.bankclient.infraestructure.persistence.document.ClientDocument;
import com.nttdata.bankclient.infraestructure.persistence.mapper.ClientMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientMapperTest {

    private ClientMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ClientMapper();
    }

    @Test
    void shouldMapPersonalCreateRequestToDomain() {
        CreateClientRequest request = new CreateClientRequest(
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Chris",
                "Vega",
                null
        );

        Client result = mapper.toDomain(request);

        assertAll(
                () -> assertNull(result.getId()),
                () -> assertEquals(
                        ClientType.PERSONAL,
                        result.getClientType()
                ),
                () -> assertEquals(
                        DocumentType.DNI,
                        result.getDocumentType()
                ),
                () -> assertEquals(
                        "71234567",
                        result.getDocumentNumber()
                ),
                () -> assertEquals(
                        "Chris",
                        result.getFirstName()
                ),
                () -> assertEquals(
                        "Vega",
                        result.getLastName()
                ),
                () -> assertNull(result.getBusinessName()),
                () -> assertTrue(result.getActive())
        );
    }

    @Test
    void shouldMapBusinessCreateRequestToDomain() {
        CreateClientRequest request = new CreateClientRequest(
                ClientType.BUSINESS,
                DocumentType.RUC,
                "20123456789",
                null,
                null,
                "Inversiones Vega S.A.C."
        );

        Client result = mapper.toDomain(request);

        assertAll(
                () -> assertNull(result.getId()),
                () -> assertEquals(
                        ClientType.BUSINESS,
                        result.getClientType()
                ),
                () -> assertEquals(
                        DocumentType.RUC,
                        result.getDocumentType()
                ),
                () -> assertEquals(
                        "20123456789",
                        result.getDocumentNumber()
                ),
                () -> assertNull(result.getFirstName()),
                () -> assertNull(result.getLastName()),
                () -> assertEquals(
                        "Inversiones Vega S.A.C.",
                        result.getBusinessName()
                ),
                () -> assertTrue(result.getActive())
        );
    }

    @Test
    void shouldMapDomainToDocument() {
        Client client = new Client(
                "client-001",
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Chris",
                "Vega",
                null,
                Boolean.TRUE
        );

        ClientDocument result = mapper.toDocument(client);

        assertAll(
                () -> assertEquals(
                        "client-001",
                        result.getId()
                ),
                () -> assertEquals(
                        ClientType.PERSONAL,
                        result.getClientType()
                ),
                () -> assertEquals(
                        DocumentType.DNI,
                        result.getDocumentType()
                ),
                () -> assertEquals(
                        "71234567",
                        result.getDocumentNumber()
                ),
                () -> assertEquals(
                        "Chris",
                        result.getFirstName()
                ),
                () -> assertEquals(
                        "Vega",
                        result.getLastName()
                ),
                () -> assertNull(result.getBusinessName()),
                () -> assertTrue(result.getActive())
        );
    }

    @Test
    void shouldMapDocumentToDomain() {
        ClientDocument document = new ClientDocument(
                "client-001",
                ClientType.BUSINESS,
                DocumentType.RUC,
                "20123456789",
                null,
                null,
                "Inversiones Vega S.A.C.",
                Boolean.TRUE
        );

        Client result = mapper.toDomain(document);

        assertAll(
                () -> assertEquals(
                        "client-001",
                        result.getId()
                ),
                () -> assertEquals(
                        ClientType.BUSINESS,
                        result.getClientType()
                ),
                () -> assertEquals(
                        DocumentType.RUC,
                        result.getDocumentType()
                ),
                () -> assertEquals(
                        "20123456789",
                        result.getDocumentNumber()
                ),
                () -> assertNull(result.getFirstName()),
                () -> assertNull(result.getLastName()),
                () -> assertEquals(
                        "Inversiones Vega S.A.C.",
                        result.getBusinessName()
                ),
                () -> assertTrue(result.getActive())
        );
    }

    @Test
    void shouldBuildPersonalDisplayNameFromDomain() {
        Client client = new Client(
                "client-001",
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234567",
                "Chris",
                "Vega",
                null,
                Boolean.TRUE
        );

        ClientResponse result = mapper.toResponse(client);

        assertAll(
                () -> assertEquals(
                        "client-001",
                        result.id()
                ),
                () -> assertEquals(
                        ClientType.PERSONAL,
                        result.clientType()
                ),
                () -> assertEquals(
                        DocumentType.DNI,
                        result.documentType()
                ),
                () -> assertEquals(
                        "71234567",
                        result.documentNumber()
                ),
                () -> assertEquals(
                        "Chris Vega",
                        result.displayName()
                ),
                () -> assertTrue(result.active())
        );
    }

    @Test
    void shouldBuildBusinessDisplayNameFromDocument() {
        ClientDocument document = new ClientDocument(
                "client-002",
                ClientType.BUSINESS,
                DocumentType.RUC,
                "20123456789",
                null,
                null,
                "Inversiones Vega S.A.C.",
                Boolean.TRUE
        );

        ClientResponse result = mapper.toResponse(document);

        assertAll(
                () -> assertEquals(
                        "client-002",
                        result.id()
                ),
                () -> assertEquals(
                        ClientType.BUSINESS,
                        result.clientType()
                ),
                () -> assertEquals(
                        DocumentType.RUC,
                        result.documentType()
                ),
                () -> assertEquals(
                        "20123456789",
                        result.documentNumber()
                ),
                () -> assertEquals(
                        "Inversiones Vega S.A.C.",
                        result.displayName()
                ),
                () -> assertTrue(result.active())
        );
    }

    @Test
    void shouldBuildPersonalDisplayNameWhenLastNameIsNull() {
        Client client = new Client(
                "client-003",
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234568",
                "Chris",
                null,
                null,
                Boolean.TRUE
        );

        ClientResponse result = mapper.toResponse(client);

        assertEquals(
                "Chris",
                result.displayName()
        );
    }

    @Test
    void shouldBuildPersonalDisplayNameWhenFirstNameIsNull() {
        Client client = new Client(
                "client-004",
                ClientType.PERSONAL,
                DocumentType.DNI,
                "71234569",
                null,
                "Vega",
                null,
                Boolean.TRUE
        );

        ClientResponse result = mapper.toResponse(client);

        assertEquals(
                "Vega",
                result.displayName()
        );
    }
}