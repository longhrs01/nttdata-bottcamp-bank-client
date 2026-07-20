package com.nttdata.bankclient.dto.response;

import com.nttdata.bankclient.domain.enums.ClientType;
import com.nttdata.bankclient.domain.enums.DocumentType;

public record ClientResponse(String id,
                             ClientType clientType,
                             DocumentType documentType,
                             String documentNumber,
                             String displayName,
                             Boolean active) {
}
