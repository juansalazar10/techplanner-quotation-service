package quotation_service.service;

import quotation_service.dto.QuotationRequest;
import quotation_service.dto.QuotationResponse;

public interface QuotationService {
    QuotationResponse createQuotation(QuotationRequest request);
}
