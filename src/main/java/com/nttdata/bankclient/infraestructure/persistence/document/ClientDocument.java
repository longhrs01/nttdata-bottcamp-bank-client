package com.nttdata.bankclient.infraestructure.persistence.document;

import com.nttdata.bankclient.domain.enums.ClientType;
import com.nttdata.bankclient.domain.enums.DocumentType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("clients")
public class ClientDocument {
    @Id
    private String id;
    private ClientType clientType;
    private DocumentType documentType;
    @Indexed(unique = true)
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String businessName;
    private Boolean active;
}
