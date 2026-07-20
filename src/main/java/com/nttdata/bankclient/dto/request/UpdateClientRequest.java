package com.nttdata.bankclient.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record UpdateClientRequest(
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,
        @Size(max = 40, message = "Last name must not exceed 40 characters")
        String lastName,
        @Size(max = 50, message = "Business must not exceed 50 characters")
        String businessName
) {
    @JsonIgnore
    @AssertTrue(message = "At least one field must be provided for update")
    public boolean isAtLeastOneFieldProvided() {
        return hasText(firstName)
                || hasText(lastName)
                || hasText(businessName);
    }

    @JsonIgnore
    @AssertTrue(message = "Provided fields must not contain only whitespace")
    public boolean areProvidedFieldsValid() {
        return isNullOrHasText(firstName)
                && isNullOrHasText(lastName)
                && isNullOrHasText(businessName);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isNullOrHasText(String value) {
        return value == null || !value.isBlank();
    }
}
