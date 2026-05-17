package quotation_service.recommendation;

import org.junit.jupiter.api.Test;
import quotation_service.dto.ComponentDto;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationServiceImplTest {

    private final RecommendationServiceImpl recommendationService = new RecommendationServiceImpl();

    @Test
    void recommendGamingBuildShouldIncludeGamingComponents() {
        List<ComponentDto> components = recommendationService.recommend("gaming", new BigDecimal("2500"));

        assertThat(components).hasSize(7);
        assertThat(components).extracting(ComponentDto::category)
                .contains("Motherboard", "CPU", "GPU", "RAM", "Storage", "PSU", "OS");
        assertThat(components).anySatisfy(component -> {
            assertThat(component.category()).isEqualTo("CPU");
            assertThat(component.model()).isEqualTo("AMD Ryzen 7 7800X");
            assertThat(component.socket()).isEqualTo("AM5");
        });
    }

    @Test
    void recommendUnknownUsageShouldFallbackToOfficeBuild() {
        List<ComponentDto> components = recommendationService.recommend("unknown", new BigDecimal("900"));

        assertThat(components).isNotEmpty();
        assertThat(components.getFirst().category()).isEqualTo("Motherboard");
        assertThat(components).anySatisfy(component ->
                assertThat(component.model()).isEqualTo("Intel Core i3 13100"));
    }
}
