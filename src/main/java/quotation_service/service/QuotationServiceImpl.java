package quotation_service.service;

import org.springframework.stereotype.Service;
import quotation_service.dto.ComponentDto;
import quotation_service.dto.CompatibilityResult;
import quotation_service.dto.QuotationRequest;
import quotation_service.dto.QuotationResponse;
import com.techplanner.recommendationlib.model.RecommendationRequest;
import com.techplanner.recommendationlib.model.RecommendationResult;
import com.techplanner.recommendationlib.service.RecommendationService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Service
public class QuotationServiceImpl implements QuotationService {

    private final RecommendationService recommendationService;
    private final CompatibilityService compatibilityService;

    public QuotationServiceImpl(RecommendationService recommendationService, CompatibilityService compatibilityService) {
        this.recommendationService = recommendationService;
        this.compatibilityService = compatibilityService;
    }

    @Override
    public QuotationResponse createQuotation(QuotationRequest request) {
        List<ComponentDto> componentsToAnalyze = request.components() == null || request.components().isEmpty()
            ? mapToComponents(recommendationService.recommend(new RecommendationRequest(request.usageType(), request.budget())))
            : request.components();

        BigDecimal total = componentsToAnalyze.stream()
                .map(ComponentDto::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean withinBudget = request.budget() == null || total.compareTo(request.budget()) <= 0;
        CompatibilityResult compatibility = compatibilityService.validate(componentsToAnalyze);

        List<String> notes = new ArrayList<>();
        if (!withinBudget) {
            notes.add("La configuración recomendada excede el presupuesto.");
        } else {
            notes.add("Configuración dentro del presupuesto.");
        }

        if (!compatibility.compatible()) {
            notes.add("La configuración tiene incompatibilidades de hardware.");
        }

        return new QuotationResponse(
            request.usageType(),
            LocalDateTime.now(),
            componentsToAnalyze,
            total,
            withinBudget,
            notes,
            compatibility
        );
    }

    private List<ComponentDto> mapToComponents(RecommendationResult recommendationResult) {
        return recommendationResult.components().stream()
                .map(component -> new ComponentDto(
                        component.category(),
                        component.model(),
                        component.price(),
                        component.socket(),
                        component.ramType(),
                        component.capacityGb(),
                        component.powerConsumptionWatts(),
                        component.psuWattage(),
                        component.maxRamGb(),
                        component.storageInterface(),
                        component.supportedSockets(),
                        component.supportedRamTypes(),
                        component.supportedStorageInterfaces()
                ))
                .toList();
    }
}
