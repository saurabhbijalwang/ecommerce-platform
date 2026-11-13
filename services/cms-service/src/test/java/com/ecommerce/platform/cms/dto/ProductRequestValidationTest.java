package com.ecommerce.platform.cms.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProductRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void acceptsValidCatalogFieldsAndNestedMetadata() {
        ProductRequest request = new ProductRequest(
            "PHONE-001",
            "Phone",
            null,
            "PHONE-001-BLK",
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("899.9900"),
            "USD",
            Map.of("color", "black"),
            List.of(new ProductRequest.VariantRequest("PHONE-001-WHT", "White", null, Map.of())),
            List.of(new ProductRequest.MediaRequest(
                "https://cdn.example.test/phone.png",
                "Front view",
                com.ecommerce.platform.cms.entity.ProductMediaType.IMAGE,
                0
            )),
            null
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsNonPositivePriceAndInvalidCurrency() {
        ProductRequest request = new ProductRequest(
            "PHONE-001",
            "Phone",
            null,
            "PHONE-001-BLK",
            UUID.randomUUID(),
            UUID.randomUUID(),
            BigDecimal.ZERO,
            "usd",
            Map.of(),
            List.of(
                new ProductRequest.VariantRequest("DUPLICATE-SKU", "First", null, Map.of()),
                new ProductRequest.VariantRequest("DUPLICATE-SKU", "Second", null, Map.of())
            ),
            List.of(),
            null
        );

        assertThat(validator.validate(request))
            .extracting(violation -> violation.getPropertyPath().toString())
            .contains("price", "currency");
    }
}
