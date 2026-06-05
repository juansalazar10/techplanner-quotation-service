package quotation_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import quotation_service.dto.ComponentDto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RecommendationProcessService {

    private final ObjectMapper objectMapper;
    private final String executablePath;
    private final long timeoutSeconds;

    public RecommendationProcessService(
            ObjectMapper objectMapper,
            @Value("${recommendation.lib.executable:recommendation-lib.exe}") String executablePath,
            @Value("${recommendation.lib.timeout-seconds:30}") long timeoutSeconds
    ) {
        this.objectMapper = objectMapper;
        this.executablePath = executablePath;
        this.timeoutSeconds = timeoutSeconds;
    }

    public List<ComponentDto> recommend(String usageType, BigDecimal budget) {
        List<String> command = new ArrayList<>();
        command.add(executablePath);
        command.add(usageType);
        if (budget != null) {
            command.add(budget.toPlainString());
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process;
        String output;
try {
    System.out.println("=== EXECUTABLE ===");
    System.out.println(executablePath);

    process = processBuilder.start();

    output = readProcessOutput(process);

    System.out.println("=== OUTPUT ===");
    System.out.println(output);

} catch (IOException exception) {

    exception.printStackTrace();

    throw new RecommendationProcessException(
            "No se pudo ejecutar recommendation-lib.exe",
            exception
    );
}

        boolean finished;
        try {
            finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RecommendationProcessException("La ejecución de recommendation-lib.exe fue interrumpida", exception);
        }

        if (!finished) {
            process.destroyForcibly();
            throw new RecommendationProcessException(
                    "La ejecución de recommendation-lib.exe superó el tiempo límite de " + Duration.ofSeconds(timeoutSeconds)
            );
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new RecommendationProcessException(
                    "recommendation-lib.exe finalizó con código de salida " + exitCode + ": " + output
            );
        }

        return parseComponents(output);
    }

    private String readProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!builder.isEmpty()) {
                    builder.append(System.lineSeparator());
                }
                builder.append(line);
            }
            return builder.toString();
        }
    }

    private List<ComponentDto> parseComponents(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode componentsNode = extractComponentsNode(root);

            if (componentsNode == null || !componentsNode.isArray()) {
                throw new RecommendationProcessException("La salida JSON no contiene una lista de componentes válida");
            }

            List<ComponentDto> components = new ArrayList<>();
            for (JsonNode componentNode : componentsNode) {
                components.add(objectMapper.treeToValue(componentNode, ComponentDto.class));
            }
            return components;
        } catch (JsonProcessingException exception) {
            throw new RecommendationProcessException("La salida de recommendation-lib.exe no es JSON válido", exception);
        }
    }

    private JsonNode extractComponentsNode(JsonNode root) {
        if (root == null) {
            return null;
        }

        if (root.isArray()) {
            return root;
        }

        JsonNode components = root.get("components");
        if (components != null) {
            return components;
        }

        JsonNode recommendations = root.get("recommendations");
        if (recommendations != null) {
            return recommendations;
        }

        JsonNode data = root.get("data");
        if (data != null && data.isArray()) {
            return data;
        }

        return null;
    }
}