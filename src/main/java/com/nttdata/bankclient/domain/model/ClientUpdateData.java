package com.nttdata.bankclient.domain.model;

public final class ClientUpdateData {

    private final String firstName;
    private final String lastName;
    private final String businessName;

    public ClientUpdateData(
            String firstName,
            String lastName,
            String businessName) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.businessName = businessName;
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
}