package quotation_service.service;

import quotation_service.dto.ComponentDto;
import quotation_service.dto.CompatibilityResult;

import java.util.List;

public interface CompatibilityService {
    CompatibilityResult validate(List<ComponentDto> components);
}
