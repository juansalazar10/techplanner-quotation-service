package quotation_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record QuotationResponse(
        String usageType,
        LocalDateTime generatedAt,
        List<ComponentDto> recommendedConfiguration,
        BigDecimal totalPrice,
        boolean withinBudget,
        List<String> notes,
        CompatibilityResult compatibility
) {
}
