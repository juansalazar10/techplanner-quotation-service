package quotation_service.service;

import org.springframework.stereotype.Service;
import quotation_service.dto.ComponentDto;
import quotation_service.dto.CompatibilityResult;
import quotation_service.dto.QuotationRequest;
import quotation_service.dto.QuotationResponse;
import quotation_service.recommendation.RecommendationService;

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
                ? recommendationService.recommend(request.usageType(), request.budget())
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
}
