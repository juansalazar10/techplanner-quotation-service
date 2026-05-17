package quotation_service.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import quotation_service.dto.ComponentDto;
import quotation_service.dto.QuotationResponse;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfServiceImpl implements PdfService {

    private static final float PAGE_MARGIN = 48f;
    private static final float LEADING = 16f;
    private static final float SMALL_LEADING = 12f;
    private static final float TITLE_SIZE = 20f;
    private static final float SECTION_SIZE = 13f;
    private static final float BODY_SIZE = 10f;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final PDFont FONT_TITLE = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont FONT_SECTION = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont FONT_BODY = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FONT_FOOTER = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    @Override
    public byte[] generateQuotationPdf(QuotationResponse quotationResponse) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - PAGE_MARGIN;

                y = drawHeader(contentStream, y, quotationResponse);
                y = drawSummary(contentStream, y, quotationResponse);
                y = drawComponents(contentStream, y, quotationResponse.recommendedConfiguration());
                y = drawNotes(contentStream, y, quotationResponse.notes(), "Recomendaciones");
                y = drawCompatibility(contentStream, y, quotationResponse);
                drawFooter(contentStream, quotationResponse, page);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible generar el PDF de cotización", exception);
        }
    }

    private float drawHeader(PDPageContentStream contentStream, float y, QuotationResponse response) throws IOException {
        y = drawTitle(contentStream, y, "Cotización TechPlanner", FONT_TITLE, TITLE_SIZE);
        y = drawLine(contentStream, y - 6f);
        y = drawText(contentStream, y, "Tipo de uso: " + safeText(response.usageType()));
        y = drawText(contentStream, y, "Fecha: " + formatDateTime(response.generatedAt()));
        y = y - 6f;
        return y;
    }

    private float drawSummary(PDPageContentStream contentStream, float y, QuotationResponse response) throws IOException {
        y = drawSectionTitle(contentStream, y, "Resumen de cotización");
        y = drawText(contentStream, y, "Total estimado: " + formatCurrency(response.totalPrice()));
        y = drawText(contentStream, y, "Dentro del presupuesto: " + (response.withinBudget() ? "Sí" : "No"));

        String operatingSystem = findComponent(response.recommendedConfiguration(), "OS");
        y = drawText(contentStream, y, "Sistema operativo: " + operatingSystem);
        return y - 4f;
    }

    private float drawComponents(PDPageContentStream contentStream, float y, List<ComponentDto> components) throws IOException {
        y = drawSectionTitle(contentStream, y, "Componentes seleccionados");

        for (ComponentDto component : components) {
            if (y < 100f) {
                y = addPageBreak(contentStream, y);
            }

            y = drawText(contentStream, y, component.category() + ": " + safeText(component.model()) + " - " + formatCurrency(component.price()));
        }

        return y - 4f;
    }

    private float drawNotes(PDPageContentStream contentStream, float y, List<String> notes, String title) throws IOException {
        y = drawSectionTitle(contentStream, y, title);

        if (notes == null || notes.isEmpty()) {
            return drawText(contentStream, y, "Sin observaciones registradas.") - 4f;
        }

        for (String note : notes) {
            if (y < 100f) {
                y = addPageBreak(contentStream, y);
            }
            y = drawText(contentStream, y, "- " + safeText(note));
        }

        return y - 4f;
    }

    private float drawCompatibility(PDPageContentStream contentStream, float y, QuotationResponse response) throws IOException {
        y = drawSectionTitle(contentStream, y, "Compatibilidades detectadas");

        if (response.compatibility() == null) {
            return drawText(contentStream, y, "No se pudo evaluar la compatibilidad.") - 4f;
        }

        y = drawText(contentStream, y, "Compatible: " + (response.compatibility().compatible() ? "Sí" : "No"));
        y = drawText(contentStream, y, "Consumo energético estimado: " + safeNumber(response.compatibility().estimatedPowerConsumptionWatts()) + " W");
        y = drawText(contentStream, y, "Fuente recomendada: " + safeNumber(response.compatibility().recommendedPsuWatts()) + " W");

        List<String> incompatibilities = response.compatibility().incompatibilities();
        if (incompatibilities == null || incompatibilities.isEmpty()) {
            return drawText(contentStream, y, "Sin incompatibilidades detectadas.") - 4f;
        }

        y = drawText(contentStream, y, "Incompatibilidades:");
        for (String incompatibility : incompatibilities) {
            if (y < 100f) {
                y = addPageBreak(contentStream, y);
            }
            y = drawText(contentStream, y, "- " + safeText(incompatibility));
        }

        return y - 4f;
    }

    private void drawFooter(PDPageContentStream contentStream, QuotationResponse response, PDPage page) throws IOException {
        contentStream.beginText();
        contentStream.setFont(FONT_FOOTER, 8f);
        contentStream.newLineAtOffset(PAGE_MARGIN, 24f);
        contentStream.showText("TechPlanner quotation-service - PDF generado automáticamente");
        contentStream.endText();
    }

    private float drawSectionTitle(PDPageContentStream contentStream, float y, String title) throws IOException {
        y = drawText(contentStream, y, title, FONT_SECTION, SECTION_SIZE);
        return y - 2f;
    }

    private float drawTitle(PDPageContentStream contentStream, float y, String text, PDFont font, float size) throws IOException {
        return drawText(contentStream, y, text, font, size);
    }

    private float drawLine(PDPageContentStream contentStream, float y) throws IOException {
        contentStream.setStrokingColor(new Color(176, 176, 176));
        contentStream.moveTo(PAGE_MARGIN, y);
        contentStream.lineTo(PDRectangle.A4.getWidth() - PAGE_MARGIN, y);
        contentStream.stroke();
        contentStream.setStrokingColor(Color.BLACK);
        return y - 12f;
    }

    private float drawText(PDPageContentStream contentStream, float y, String text) throws IOException {
        return drawText(contentStream, y, text, FONT_BODY, BODY_SIZE);
    }

    private float drawText(PDPageContentStream contentStream, float y, String text, PDFont font, float size) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, size);
        contentStream.newLineAtOffset(PAGE_MARGIN, y);
        contentStream.showText(truncate(text));
        contentStream.endText();
        return y - (size + 4f);
    }

    private float addPageBreak(PDPageContentStream contentStream, float y) throws IOException {
        contentStream.stroke();
        return y;
    }

    private String findComponent(List<ComponentDto> components, String category) {
        if (components == null) {
            return "No especificado";
        }

        return components.stream()
                .filter(component -> component.category() != null && component.category().equalsIgnoreCase(category))
                .map(component -> safeText(component.model()))
                .findFirst()
                .orElse("No especificado");
    }

    private String formatCurrency(BigDecimal amount) {
        return amount == null ? "N/D" : "$" + amount.stripTrailingZeros().toPlainString();
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime == null ? "N/D" : dateTime.format(DATE_TIME_FORMATTER);
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "No especificado" : value;
    }

    private String safeNumber(Integer value) {
        return value == null ? "N/D" : value.toString();
    }

    private String truncate(String value) {
        String safe = safeText(value);
        return safe.length() > 110 ? safe.substring(0, 107) + "..." : safe;
    }
}
