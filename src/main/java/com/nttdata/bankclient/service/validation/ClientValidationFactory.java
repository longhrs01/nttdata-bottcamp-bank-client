package com.nttdata.bankclient.service.validation;

import com.nttdata.bankclient.domain.enums.ClientType;
import com.nttdata.bankclient.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ClientValidationFactory {
    private final Map<ClientType, ClientValidationStrategy> strategies;

    public ClientValidationFactory(
            List<ClientValidationStrategy> validationStrategies) {

        this.strategies = new EnumMap<>(ClientType.class);

        validationStrategies.forEach(strategy ->
                strategies.put(
                        strategy.supportedType(),
                        strategy
                )
        );
    }

    public ClientValidationStrategy getStrategy(ClientType clientType) {
        ClientValidationStrategy strategy = strategies.get(resolveBaseType(clientType));

        if (strategy == null) {
            throw new BusinessException(
                    "Unsupported client type: " + clientType
            );
        }

        return strategy;
    }

    private ClientType resolveBaseType(ClientType clientType) {
        if (clientType == ClientType.PERSONAL_VIP) {
            return ClientType.PERSONAL;
        }

        if (clientType == ClientType.BUSINESS_PYME) {
            return ClientType.BUSINESS;
        }

        return clientType;
    }
}
