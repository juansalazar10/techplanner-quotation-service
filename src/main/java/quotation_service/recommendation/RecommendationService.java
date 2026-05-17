package quotation_service.recommendation;

import java.math.BigDecimal;
import java.util.List;
import quotation_service.dto.ComponentDto;

public interface RecommendationService {
    List<ComponentDto> recommend(String usageType, BigDecimal budget);
}
