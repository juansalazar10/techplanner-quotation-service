package quotation_service.service;

import org.springframework.stereotype.Service;
import quotation_service.dto.ComponentDto;
import quotation_service.dto.CompatibilityResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class CompatibilityServiceImpl implements CompatibilityService {

    @Override
    public CompatibilityResult validate(List<ComponentDto> components) {
        List<ComponentDto> safeComponents = components == null ? List.of() : components;
        List<String> incompatibilities = new ArrayList<>();

        ComponentDto motherboard = firstByCategory(safeComponents, "motherboard");
        ComponentDto cpu = firstByCategory(safeComponents, "cpu");
        ComponentDto gpu = firstByCategory(safeComponents, "gpu");
        ComponentDto psu = firstByCategory(safeComponents, "psu", "fuente", "power supply");

        validateCpuAndMotherboard(cpu, motherboard, incompatibilities);
        validateRamAndMotherboard(safeComponents, motherboard, incompatibilities);
        validateGpuAndPowerSupply(gpu, psu, safeComponents, incompatibilities);
        validateStorageAndMotherboard(safeComponents, motherboard, incompatibilities);

        int estimatedPowerConsumption = safeComponents.stream()
                .map(ComponentDto::powerConsumptionWatts)
                .filter(value -> value != null && value > 0)
                .mapToInt(Integer::intValue)
                .sum();

        int recommendedPsuWatts = (int) Math.ceil(estimatedPowerConsumption * 1.25d);

        if (psu == null && estimatedPowerConsumption > 0) {
            incompatibilities.add("No se encontró una fuente de poder para validar el consumo energético estimado.");
        } else if (psu != null && psu.psuWattage() != null && estimatedPowerConsumption > psu.psuWattage()) {
            incompatibilities.add("El consumo energético estimado supera la capacidad de la fuente.");
        }

        return new CompatibilityResult(
                incompatibilities.isEmpty(),
                List.copyOf(incompatibilities),
                estimatedPowerConsumption,
                recommendedPsuWatts
        );
    }

    private void validateCpuAndMotherboard(ComponentDto cpu, ComponentDto motherboard, List<String> incompatibilities) {
        if (cpu == null || motherboard == null) {
            return;
        }

        if (hasText(cpu.socket()) && motherboard.supportedSockets() != null && !motherboard.supportedSockets().isEmpty()) {
            boolean socketCompatible = motherboard.supportedSockets().stream()
                    .filter(this::hasText)
                    .map(this::normalize)
                    .anyMatch(socket -> socket.equals(normalize(cpu.socket())));

            if (!socketCompatible) {
                incompatibilities.add("La motherboard no soporta el socket del procesador.");
            }
        }
    }

    private void validateRamAndMotherboard(List<ComponentDto> components, ComponentDto motherboard, List<String> incompatibilities) {
        List<ComponentDto> rams = components.stream()
                .filter(component -> isCategory(component.category(), "ram"))
                .toList();

        if (rams.isEmpty() || motherboard == null) {
            return;
        }

        int totalRamCapacityGb = rams.stream()
                .map(ComponentDto::capacityGb)
                .filter(value -> value != null && value > 0)
                .mapToInt(Integer::intValue)
                .sum();

        if (motherboard.maxRamGb() != null && totalRamCapacityGb > motherboard.maxRamGb()) {
            incompatibilities.add("La RAM excede la capacidad soportada por la motherboard.");
        }

        if (motherboard.supportedRamTypes() != null && !motherboard.supportedRamTypes().isEmpty()) {
            boolean anyRamTypeCompatible = rams.stream()
                    .map(ComponentDto::ramType)
                    .filter(this::hasText)
                    .map(this::normalize)
                    .allMatch(ramType -> motherboard.supportedRamTypes().stream()
                            .filter(this::hasText)
                            .map(this::normalize)
                            .anyMatch(ramType::equals));

            if (!anyRamTypeCompatible) {
                incompatibilities.add("La motherboard no soporta el tipo de RAM seleccionado.");
            }
        }
    }

    private void validateGpuAndPowerSupply(ComponentDto gpu, ComponentDto psu, List<ComponentDto> components, List<String> incompatibilities) {
        if (gpu == null || psu == null || psu.psuWattage() == null || gpu.powerConsumptionWatts() == null) {
            return;
        }

        int gpuMinimumRequired = gpu.powerConsumptionWatts() + 150;
        if (psu.psuWattage() < gpuMinimumRequired) {
            incompatibilities.add("La fuente no tiene suficiente potencia para la GPU.");
        }
    }

    private void validateStorageAndMotherboard(List<ComponentDto> components, ComponentDto motherboard, List<String> incompatibilities) {
        List<ComponentDto> storageDevices = components.stream()
                .filter(component -> isCategory(component.category(), "storage", "almacenamiento", "ssd", "hdd"))
                .toList();

        if (storageDevices.isEmpty() || motherboard == null) {
            return;
        }

        if (motherboard.supportedStorageInterfaces() == null || motherboard.supportedStorageInterfaces().isEmpty()) {
            return;
        }

        for (ComponentDto storage : storageDevices) {
            if (!hasText(storage.storageInterface())) {
                continue;
            }

            boolean storageCompatible = motherboard.supportedStorageInterfaces().stream()
                    .filter(this::hasText)
                    .map(this::normalize)
                    .anyMatch(interfaceType -> interfaceType.equals(normalize(storage.storageInterface())));

            if (!storageCompatible) {
                incompatibilities.add("El almacenamiento no es compatible con la motherboard.");
                break;
            }
        }
    }

    private ComponentDto firstByCategory(List<ComponentDto> components, String... categories) {
        return components.stream()
                .filter(component -> isCategory(component.category(), categories))
                .findFirst()
                .orElse(null);
    }

    private boolean isCategory(String category, String... categories) {
        if (!hasText(category)) {
            return false;
        }

        String normalizedCategory = normalize(category);
        for (String candidate : categories) {
            if (normalizedCategory.equals(normalize(candidate))) {
                return true;
            }
        }

        return false;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
