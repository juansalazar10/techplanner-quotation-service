package quotation_service.service;

import org.junit.jupiter.api.Test;
import quotation_service.dto.ComponentDto;
import quotation_service.dto.CompatibilityResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompatibilityServiceImplTest {

    private final CompatibilityServiceImpl compatibilityService = new CompatibilityServiceImpl();

    @Test
    void validateShouldAcceptCompatibleConfiguration() {
        List<ComponentDto> components = List.of(
                new ComponentDto("Motherboard", "H610", BigDecimal.valueOf(110), null, null, null, 25, null, 64, null, List.of("LGA1700"), List.of("DDR4"), List.of("SATA", "NVMe")),
                new ComponentDto("CPU", "Intel Core i3 13100", BigDecimal.valueOf(120), "LGA1700", null, null, 60, null, null, null, null, null, null),
                new ComponentDto("GPU", "NVIDIA RTX 4060", BigDecimal.valueOf(300), null, null, null, 120, null, null, null, null, null, null),
                new ComponentDto("RAM", "16GB DDR4", BigDecimal.valueOf(50), null, "DDR4", 16, 8, null, null, null, null, null, null),
                new ComponentDto("Storage", "512GB SSD", BigDecimal.valueOf(40), null, null, 512, 5, null, null, "SATA", null, null, null),
                new ComponentDto("PSU", "550W Bronze", BigDecimal.valueOf(75), null, null, null, null, 550, null, null, null, null, null)
        );

        CompatibilityResult result = compatibilityService.validate(components);

        assertThat(result.compatible()).isTrue();
        assertThat(result.incompatibilities()).isEmpty();
        assertThat(result.estimatedPowerConsumptionWatts()).isEqualTo(218);
        assertThat(result.recommendedPsuWatts()).isEqualTo(273);
    }

    @Test
    void validateShouldReportSocketAndPowerIncompatibilities() {
        List<ComponentDto> components = List.of(
                new ComponentDto("Motherboard", "H610", BigDecimal.valueOf(110), null, null, null, 25, null, 64, null, List.of("LGA1700"), List.of("DDR4"), List.of("SATA", "NVMe")),
                new ComponentDto("CPU", "AMD Ryzen 7 7800X", BigDecimal.valueOf(320), "AM5", null, null, 105, null, null, null, null, null, null),
                new ComponentDto("GPU", "NVIDIA RTX 4080", BigDecimal.valueOf(1200), null, null, null, 320, null, null, null, null, null, null),
                new ComponentDto("RAM", "16GB DDR4", BigDecimal.valueOf(50), null, "DDR4", 16, 8, null, null, null, null, null, null),
                new ComponentDto("Storage", "512GB SSD", BigDecimal.valueOf(40), null, null, 512, 5, null, null, "SATA", null, null, null),
                new ComponentDto("PSU", "400W Bronze", BigDecimal.valueOf(60), null, null, null, null, 400, null, null, null, null, null)
        );

        CompatibilityResult result = compatibilityService.validate(components);

        assertThat(result.compatible()).isFalse();
        assertThat(result.incompatibilities())
                .anySatisfy(message -> assertThat(message).contains("socket del procesador"))
                .anySatisfy(message -> assertThat(message).contains("suficiente potencia"));
    }
}
