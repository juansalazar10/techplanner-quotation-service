package quotation_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import quotation_service.dto.ComponentDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationProcessService {

    private final String executablePath;

    public RecommendationProcessService(
            @Value("${recommendation.lib.executable:disabled}") String executablePath
    ) {
        this.executablePath = executablePath;

        System.out.println("=================================");
        System.out.println("RECOMMENDATION SERVICE INICIADO");
        System.out.println("MODE: " + executablePath);
        System.out.println("=================================");
    }

    public List<ComponentDto> recommend(String usageType, BigDecimal budget) {

        System.out.println("=================================");
        System.out.println("RECOMMENDATION MOCK ACTIVADO");
        System.out.println("USAGE: " + usageType);
        System.out.println("BUDGET: " + budget);
        System.out.println("=================================");

        // 🔥 SIMULACIÓN DE RESPUESTA (para que todo funcione ya)
        List<ComponentDto> components = new ArrayList<>();

        components.add(new ComponentDto(
                "CPU",
                "AMD Ryzen 7",
                BigDecimal.valueOf(320),
                "AM5",
                null,
                null,
                105,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        components.add(new ComponentDto(
                "GPU",
                "RTX 4070",
                BigDecimal.valueOf(600),
                null,
                null,
                null,
                200,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        return components;
    }
}