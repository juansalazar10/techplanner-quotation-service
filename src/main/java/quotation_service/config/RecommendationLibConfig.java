package quotation_service.config;

import com.techplanner.recommendationlib.service.DefaultRecommendationService;
import com.techplanner.recommendationlib.service.RecommendationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecommendationLibConfig {

    @Bean
    public RecommendationService recommendationService() {
        return new DefaultRecommendationService();
    }
}
