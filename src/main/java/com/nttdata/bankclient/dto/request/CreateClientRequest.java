package com.nttdata.bankclient.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nttdata.bankclient.domain.enums.ClientType;
import com.nttdata.bankclient.domain.enums.DocumentType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateClientRequest(
        @NotNull(message = "Client type is required")
        ClientType clientType,
        @NotNull(message = "Document type is required")
        DocumentType documentType,
        @NotBlank(message = "Document number is required")
        @Size(max = 20, message = "Document number must not exceed 20 characters")
        String documentNumber,
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,
        @Size(max = 40, message = "Last name must not exceed 40 characters")
        String lastName,
        @Size(max = 50, message = "Business must not exceed 50 characters")
        String businessName
) {

    @JsonIgnore
    @AssertTrue(message = "Personal clients must have first name and last name and cannot have business name")
    public boolean isValidPersonalClient() {
        if (!isPersonalClient()) {
            return true;
        }

        return !hasText(businessName) && hasText(firstName) && hasText(lastName);
    }

    @JsonIgnore
    @AssertTrue(message = "Business clients must have business name and cannot have first name or last name")
    public boolean isValidBusinessClient() {
        if (!isBusinessClient()) {
            return true;
        }

        return hasText(businessName) && !hasText(firstName) && !hasText(lastName);
    }

    private boolean isPersonalClient() {
        return clientType == ClientType.PERSONAL
                || clientType == ClientType.PERSONAL_VIP;
    }

    private boolean isBusinessClient() {
        return clientType == ClientType.BUSINESS
                || clientType == ClientType.BUSINESS_PYME;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
