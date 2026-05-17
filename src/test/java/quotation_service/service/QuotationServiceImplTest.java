package quotation_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import quotation_service.dto.ComponentDto;
import quotation_service.dto.CompatibilityResult;
import quotation_service.dto.QuotationRequest;
import quotation_service.dto.QuotationResponse;
import com.techplanner.recommendationlib.model.RecommendationRequest;
import com.techplanner.recommendationlib.model.RecommendationResult;
import com.techplanner.recommendationlib.model.ComponentRecommendation;
import com.techplanner.recommendationlib.model.UsageType;
import com.techplanner.recommendationlib.service.RecommendationService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuotationServiceImplTest {

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private CompatibilityService compatibilityService;

    @InjectMocks
    private QuotationServiceImpl quotationService;

    @Test
    void createQuotationShouldCalculateTotalAndIncludeCompatibilityResult() {
        List<ComponentRecommendation> recommended = List.of(
            new ComponentRecommendation("CPU", "Intel Core i3", BigDecimal.valueOf(100), "LGA1700", null, null, 60, null, null, null, null, null, null),
            new ComponentRecommendation("RAM", "16GB DDR4", BigDecimal.valueOf(50), null, "DDR4", 16, 8, null, null, null, null, null, null)
        );
        CompatibilityResult compatibilityResult = new CompatibilityResult(true, List.of(), 68, 85);

        when(recommendationService.recommend(new RecommendationRequest("oficina", BigDecimal.valueOf(200))))
            .thenReturn(new RecommendationResult(UsageType.OFFICE, recommended, BigDecimal.valueOf(150), List.of("Configuración dentro del presupuesto estimado.")));
        when(compatibilityService.validate(List.of(
            new ComponentDto("CPU", "Intel Core i3", BigDecimal.valueOf(100), "LGA1700", null, null, 60, null, null, null, null, null, null),
            new ComponentDto("RAM", "16GB DDR4", BigDecimal.valueOf(50), null, "DDR4", 16, 8, null, null, null, null, null, null)
        ))).thenReturn(compatibilityResult);

        QuotationRequest request = new QuotationRequest("oficina", BigDecimal.valueOf(200), null);
        QuotationResponse response = quotationService.createQuotation(request);

        assertThat(response.usageType()).isEqualTo("oficina");
        assertThat(response.generatedAt()).isNotNull();
        assertThat(response.totalPrice()).isEqualByComparingTo("150");
        assertThat(response.withinBudget()).isTrue();
        assertThat(response.notes()).contains("Configuración dentro del presupuesto.");
        assertThat(response.compatibility()).isEqualTo(compatibilityResult);
        verify(recommendationService).recommend(new RecommendationRequest("oficina", BigDecimal.valueOf(200)));
        verify(compatibilityService).validate(List.of(
            new ComponentDto("CPU", "Intel Core i3", BigDecimal.valueOf(100), "LGA1700", null, null, 60, null, null, null, null, null, null),
            new ComponentDto("RAM", "16GB DDR4", BigDecimal.valueOf(50), null, "DDR4", 16, 8, null, null, null, null, null, null)
        ));
    }

    @Test
    void createQuotationShouldUseProvidedComponentsWhenPresent() {
        List<ComponentDto> providedComponents = List.of(
                new ComponentDto("CPU", "AMD Ryzen 7", BigDecimal.valueOf(320), "AM5", null, null, 105, null, null, null, null, null, null),
                new ComponentDto("PSU", "750W", BigDecimal.valueOf(100), null, null, null, null, 750, null, null, null, null, null)
        );
        CompatibilityResult compatibilityResult = new CompatibilityResult(false, List.of("La motherboard no soporta el socket del procesador."), 105, 132);

        when(compatibilityService.validate(providedComponents)).thenReturn(compatibilityResult);

        QuotationRequest request = new QuotationRequest("gaming", BigDecimal.valueOf(300), providedComponents);
        QuotationResponse response = quotationService.createQuotation(request);

        assertThat(response.totalPrice()).isEqualByComparingTo("420");
        assertThat(response.withinBudget()).isFalse();
        assertThat(response.notes())
                .contains("La configuración recomendada excede el presupuesto.")
                .contains("La configuración tiene incompatibilidades de hardware.");
        verify(compatibilityService).validate(providedComponents);
    }
}
