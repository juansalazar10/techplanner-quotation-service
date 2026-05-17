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
import quotation_service.recommendation.RecommendationService;
import quotation_service.service.QuotationService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/quotations")
@Validated
public class QuotationController {

    private final QuotationService quotationService;
    private final RecommendationService recommendationService;
    private final PdfService pdfService;

    public QuotationController(QuotationService quotationService, RecommendationService recommendationService, PdfService pdfService) {
        this.quotationService = quotationService;
        this.recommendationService = recommendationService;
        this.pdfService = pdfService;
    }

    @PostMapping
    public ResponseEntity<QuotationResponse> createQuotation(@Valid @RequestBody QuotationRequest request) {
        QuotationResponse response = quotationService.createQuotation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/recommendation")
    public ResponseEntity<List<ComponentDto>> recommendation(
            @RequestParam @NotBlank String usage,
            @RequestParam(required = false) BigDecimal budget
    ) {
        List<ComponentDto> rec = recommendationService.recommend(usage, budget);
        return ResponseEntity.ok(rec);
    }

    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generatePdf(@Valid @RequestBody QuotationRequest request) {
        QuotationResponse quotationResponse = quotationService.createQuotation(request);
        byte[] pdfBytes = pdfService.generateQuotationPdf(quotationResponse);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("quotation-techplanner.pdf").build());
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
