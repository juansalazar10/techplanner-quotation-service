package quotation_service.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import quotation_service.dto.ComponentDto;
import quotation_service.dto.QuotationRequest;
import quotation_service.dto.QuotationResponse;
import quotation_service.pdf.PdfService;
import quotation_service.service.RecommendationProcessService;
import quotation_service.service.QuotationService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/quotations")
@CrossOrigin(origins = "http://localhost:4200")
public class QuotationController {


    private final QuotationService quotationService;
    private final RecommendationProcessService recommendationProcessService;
    private final PdfService pdfService;

    public QuotationController(
            QuotationService quotationService,
            RecommendationProcessService recommendationProcessService,
            PdfService pdfService
    ) {
        this.quotationService = quotationService;
        this.recommendationProcessService = recommendationProcessService;
        this.pdfService = pdfService;
    }

    // =========================================
    // 1. CREAR COTIZACIÓN (NORMAL)
    // =========================================
    @PostMapping
    public ResponseEntity<QuotationResponse> createQuotation(
            @Valid @RequestBody QuotationRequest request
    ) {
        QuotationResponse response = quotationService.createQuotation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================================
    // 2. RECOMENDACIONES (YA LO TIENES)
    // =========================================
    @GetMapping("/recommendation")
    public ResponseEntity<List<ComponentDto>> recommendation(
            @RequestParam @NotBlank String usage,
            @RequestParam(required = false) BigDecimal budget
    ) {
        return ResponseEntity.ok(
                recommendationProcessService.recommend(usage, budget)
        );
    }

    // =========================================
    // 3. GENERAR PDF (MICROSERVICIO REAL)
    // =========================================
    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generatePdf(
            @Valid @RequestBody QuotationRequest request
    ) {

        // 1. Crear cotización real en backend
        QuotationResponse quotationResponse =
                quotationService.createQuotation(request);

        // 2. Generar PDF desde el microservicio
        byte[] pdfBytes =
                pdfService.generateQuotationPdf(quotationResponse);

        // 3. Datos para nombre del archivo
        String usage = request.usageType() != null
                ? request.usageType()
                : "general";

        String budget = request.budget() != null
                ? request.budget().toPlainString()
                : "no-budget";

        String filename = String.format(
                "quotation-%s-%s.pdf",
                usage,
                budget
        );

        // 4. Headers HTTP para descarga PDF
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(filename)
                        .build()
        );

        headers.setContentLength(pdfBytes.length);

        // 5. Respuesta final
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdfBytes);
    }
}