package com.nttdata.bankclient.domain.model;

import com.nttdata.bankclient.domain.enums.ClientType;
import com.nttdata.bankclient.domain.enums.DocumentType;

public class Client {

    private String id;
    private ClientType clientType;
    private DocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String businessName;
    private Boolean active;

    public Client() {
    }

    public Client(
            String id,
            ClientType clientType,
            DocumentType documentType,
            String documentNumber,
            String firstName,
            String lastName,
            String businessName,
            Boolean active) {

        this.id = id;
        this.clientType = clientType;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.businessName = businessName;
        this.active = active;
    }

    public String getId() {
        return id;
    }
    public ClientType getClientType() {
        return clientType;
    }
    public DocumentType getDocumentType() {
        return documentType;
    }
    public String getDocumentNumber() {
        return documentNumber;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getBusinessName() {
        return businessName;
    }
    public Boolean getActive() {
        return active;
    }
    public void updatePersonalInformation(
            String firstName,
            String lastName) {

        if ( hasText(firstName) ) {
            this.firstName = firstName;
        }

        if (hasText(lastName) ) {
            this.lastName = lastName;
        }
    }

    public void updateBusinessInformation(String businessName) {
        if (hasText(businessName)) {
            this.businessName = businessName.trim();
        }
    }

    public void deactivate() {
        this.active = Boolean.FALSE;
    }

    private boolean hasText(String value) {

        return value != null && !value.isBlank();
    }
}