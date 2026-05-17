package quotation_service.recommendation;

import org.springframework.stereotype.Service;
import quotation_service.dto.ComponentDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Override
    public List<ComponentDto> recommend(String usageType, BigDecimal budget) {
        var list = new ArrayList<ComponentDto>();
        String u = usageType == null ? "" : usageType.trim().toLowerCase();

        switch (u) {
            case "gaming" -> gaming(list, budget);
            case "oficina", "office" -> office(list, budget);
            case "diseño", "diseno", "design" -> design(list, budget);
            case "servidores", "server", "servers" -> servers(list, budget);
            case "presupuesto", "budget", "barato" -> budgetBuild(list, budget);
            default -> office(list, budget);
        }

        return list;
    }

    private void gaming(List<ComponentDto> out, BigDecimal budget) {
        out.add(new ComponentDto("Motherboard", "B650 ATX", new BigDecimal("220"), null, null, null, 40, null, 192, null, List.of("AM5"), List.of("DDR5"), List.of("NVMe", "SATA")));
        out.add(new ComponentDto("CPU", "AMD Ryzen 7 7800X", new BigDecimal("320"), "AM5", null, null, 105, null, null, null, null, null, null));
        out.add(new ComponentDto("GPU", "NVIDIA RTX 4070", new BigDecimal("600"), null, null, null, 200, null, null, null, null, null, null));
        out.add(new ComponentDto("RAM", "32GB DDR5", new BigDecimal("120"), null, "DDR5", 32, 10, null, null, null, null, null, null));
        out.add(new ComponentDto("Storage", "1TB NVMe", new BigDecimal("80"), null, null, 1000, 5, null, null, "NVMe", null, null, null));
        out.add(new ComponentDto("PSU", "750W Gold", new BigDecimal("110"), null, null, null, null, 750, null, null, null, null, null));
        out.add(new ComponentDto("OS", "Windows 11 Home", new BigDecimal("120"), null, null, null, 0, null, null, null, null, null, null));
    }

    private void office(List<ComponentDto> out, BigDecimal budget) {
        out.add(new ComponentDto("Motherboard", "H610 mATX", new BigDecimal("110"), null, null, null, 25, null, 64, null, List.of("LGA1700"), List.of("DDR4"), List.of("SATA", "NVMe")));
        out.add(new ComponentDto("CPU", "Intel Core i3 13100", new BigDecimal("120"), "LGA1700", null, null, 60, null, null, null, null, null, null));
        out.add(new ComponentDto("GPU", "Integrated", new BigDecimal("0"), null, null, null, 0, null, null, null, null, null, null));
        out.add(new ComponentDto("RAM", "16GB DDR4", new BigDecimal("50"), null, "DDR4", 16, 8, null, null, null, null, null, null));
        out.add(new ComponentDto("Storage", "512GB SSD", new BigDecimal("40"), null, null, 512, 5, null, null, "SATA", null, null, null));
        out.add(new ComponentDto("PSU", "450W Bronze", new BigDecimal("60"), null, null, null, null, 450, null, null, null, null, null));
        out.add(new ComponentDto("OS", "Windows 11 Pro", new BigDecimal("140"), null, null, null, 0, null, null, null, null, null, null));
    }

    private void design(List<ComponentDto> out, BigDecimal budget) {
        out.add(new ComponentDto("Motherboard", "Z790 ATX", new BigDecimal("260"), null, null, null, 35, null, 192, null, List.of("LGA1700"), List.of("DDR5"), List.of("NVMe", "SATA")));
        out.add(new ComponentDto("CPU", "Intel Core i9 13900K", new BigDecimal("560"), "LGA1700", null, null, 125, null, null, null, null, null, null));
        out.add(new ComponentDto("GPU", "NVIDIA RTX 4080", new BigDecimal("1200"), null, null, null, 320, null, null, null, null, null, null));
        out.add(new ComponentDto("RAM", "64GB DDR5", new BigDecimal("280"), null, "DDR5", 64, 18, null, null, null, null, null, null));
        out.add(new ComponentDto("Storage", "2TB NVMe", new BigDecimal("220"), null, null, 2000, 8, null, null, "NVMe", null, null, null));
        out.add(new ComponentDto("PSU", "1000W Gold", new BigDecimal("180"), null, null, null, null, 1000, null, null, null, null, null));
        out.add(new ComponentDto("OS", "Windows 11 Pro", new BigDecimal("140"), null, null, null, 0, null, null, null, null, null, null));
    }

    private void servers(List<ComponentDto> out, BigDecimal budget) {
        out.add(new ComponentDto("Motherboard", "SP5 Server Board", new BigDecimal("850"), null, null, null, 60, null, 1024, null, List.of("SP5"), List.of("DDR5 ECC"), List.of("NVMe", "SATA")));
        out.add(new ComponentDto("CPU", "AMD EPYC (multi-socket)", new BigDecimal("2000"), "SP5", null, null, 280, null, null, null, null, null, null));
        out.add(new ComponentDto("GPU", "None / Optional", new BigDecimal("0"), null, null, null, 0, null, null, null, null, null, null));
        out.add(new ComponentDto("RAM", "128GB ECC", new BigDecimal("800"), null, "DDR5 ECC", 128, 30, null, null, null, null, null, null));
        out.add(new ComponentDto("Storage", "4TB SATA RAID", new BigDecimal("400"), null, null, 4000, 20, null, null, "SATA", null, null, null));
        out.add(new ComponentDto("PSU", "1200W Platinum", new BigDecimal("250"), null, null, null, null, 1200, null, null, null, null, null));
        out.add(new ComponentDto("OS", "Linux (Ubuntu Server)", new BigDecimal("0"), null, null, null, 0, null, null, null, null, null, null));
    }

    private void budgetBuild(List<ComponentDto> out, BigDecimal budget) {
        out.add(new ComponentDto("Motherboard", "H610 Basic", new BigDecimal("90"), null, null, null, 20, null, 64, null, List.of("LGA1700"), List.of("DDR4"), List.of("SATA", "NVMe")));
        out.add(new ComponentDto("CPU", "Intel Pentium Gold", new BigDecimal("70"), "LGA1700", null, null, 46, null, null, null, null, null, null));
        out.add(new ComponentDto("GPU", "Integrated", new BigDecimal("0"), null, null, null, 0, null, null, null, null, null, null));
        out.add(new ComponentDto("RAM", "8GB DDR4", new BigDecimal("25"), null, "DDR4", 8, 5, null, null, null, null, null, null));
        out.add(new ComponentDto("Storage", "256GB SSD", new BigDecimal("25"), null, null, 256, 3, null, null, "SATA", null, null, null));
        out.add(new ComponentDto("PSU", "400W Bronze", new BigDecimal("50"), null, null, null, null, 400, null, null, null, null, null));
        out.add(new ComponentDto("OS", "Linux (Ubuntu)", new BigDecimal("0"), null, null, null, 0, null, null, null, null, null, null));
    }
}
