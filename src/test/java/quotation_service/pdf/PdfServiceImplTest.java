package quotation_service.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import quotation_service.dto.ComponentDto;
import quotation_service.dto.CompatibilityResult;
import quotation_service.dto.QuotationResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfServiceImplTest {

    private final PdfServiceImpl pdfService = new PdfServiceImpl();

    @Test
    void generateQuotationPdfShouldReturnValidPdfBytes() throws Exception {
        QuotationResponse response = new QuotationResponse(
                "gaming",
                LocalDateTime.of(2026, 5, 17, 14, 30),
                List.of(
                        new ComponentDto("CPU", "AMD Ryzen 7 7800X", BigDecimal.valueOf(320), "AM5", null, null, 105, null, null, null, null, null, null),
                        new ComponentDto("PSU", "750W Gold", BigDecimal.valueOf(110), null, null, null, null, 750, null, null, null, null, null)
                ),
                BigDecimal.valueOf(430),
                true,
                List.of("Configuración dentro del presupuesto."),
                new CompatibilityResult(true, List.of(), 105, 132)
        );

        byte[] pdfBytes = pdfService.generateQuotationPdf(response);

        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes, 0, 4)).isEqualTo("%PDF");

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(1);
        }
    }
}
