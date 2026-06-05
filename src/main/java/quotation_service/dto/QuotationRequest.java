package quotation_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record QuotationRequest(

        @NotBlank
        String usageType,

        @NotNull
        @PositiveOrZero
        BigDecimal budget,

        List<ComponentDto> components

) {
}