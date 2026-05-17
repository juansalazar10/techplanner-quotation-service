package quotation_service.dto;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record QuotationRequest(
        @NotBlank String usageType,
        @NotNull @PositiveOrZero BigDecimal budget,
        List<ComponentDto> components
) {
}
