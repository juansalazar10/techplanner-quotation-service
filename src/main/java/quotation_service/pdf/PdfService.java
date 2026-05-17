package quotation_service.pdf;

import quotation_service.dto.QuotationResponse;

public interface PdfService {
    byte[] generateQuotationPdf(QuotationResponse quotationResponse);
}
