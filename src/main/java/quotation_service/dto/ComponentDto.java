package quotation_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record ComponentDto(
        String category,
        String model,
        BigDecimal price,
        String socket,
        String ramType,
        Integer capacityGb,
        Integer powerConsumptionWatts,
        Integer psuWattage,
        Integer maxRamGb,
        String storageInterface,
        List<String> supportedSockets,
        List<String> supportedRamTypes,
        List<String> supportedStorageInterfaces
) {
}
