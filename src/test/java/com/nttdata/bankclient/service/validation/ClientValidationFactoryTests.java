package com.nttdata.bankclient.service.validation;

import com.nttdata.bankclient.domain.enums.ClientType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientValidationFactoryTests {

    private ClientValidationFactory factory;

    @BeforeEach
    void setUp() {

        factory = new ClientValidationFactory(
                List.of(
                        new PersonalClientValidationStrategy(),
                        new BusinessClientValidationStrategy()
                )
        );
    }

    @Test
    void shouldReturnPersonalStrategy() {

        ClientValidationStrategy strategy =
                factory.getStrategy(ClientType.PERSONAL);

        assertInstanceOf(
                PersonalClientValidationStrategy.class,
                strategy
        );
    }

    @Test
    void shouldReturnPersonalStrategyForVipClient() {

        ClientValidationStrategy strategy =
                factory.getStrategy(ClientType.PERSONAL_VIP);

        assertInstanceOf(
                PersonalClientValidationStrategy.class,
                strategy
        );
    }

    @Test
    void shouldReturnBusinessStrategy() {

        ClientValidationStrategy strategy =
                factory.getStrategy(ClientType.BUSINESS);

        assertInstanceOf(
                BusinessClientValidationStrategy.class,
                strategy
        );
    }

    @Test
    void shouldReturnBusinessStrategyForPymeClient() {

        ClientValidationStrategy strategy =
                factory.getStrategy(ClientType.BUSINESS_PYME);

        assertInstanceOf(
                BusinessClientValidationStrategy.class,
                strategy
        );
    }

    @Test
    void shouldReturnDifferentStrategies() {

        ClientValidationStrategy personal =
                factory.getStrategy(ClientType.PERSONAL);

        ClientValidationStrategy business =
                factory.getStrategy(ClientType.BUSINESS);

        assertNotSame(personal, business);
    }
}
