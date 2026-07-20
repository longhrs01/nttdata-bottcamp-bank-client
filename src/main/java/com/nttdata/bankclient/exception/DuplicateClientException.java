package com.nttdata.bankclient.exception;

public class DuplicateClientException extends RuntimeException {

    public DuplicateClientException(String documentNumber) {
        super(
                "Client already exists with document number: "
                        + documentNumber
        );
    }
}