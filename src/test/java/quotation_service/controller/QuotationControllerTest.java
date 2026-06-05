package quotation_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import quotation_service.dto.ComponentDto;
import quotation_service.dto.CompatibilityResult;
import quotation_service.dto.QuotationResponse;
import quotation_service.pdf.PdfService;
import quotation_service.service.QuotationService;
import quotation_service.service.RecommendationProcessService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuotationController.class)
class QuotationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuotationService quotationService;

    @MockBean
    private RecommendationProcessService recommendationProcessService;

    @MockBean
    private PdfService pdfService;

    @Test
    void createQuotationShouldReturnCreatedResponse() throws Exception {
        QuotationResponse response = new QuotationResponse(
                "gaming",
                LocalDateTime.of(2026, 5, 17, 14, 30),
                List.of(
                        new ComponentDto(
                                "CPU",
                                "AMD Ryzen 7",
                                BigDecimal.valueOf(320),
                                "AM5",
                                null,
                                null,
                                105,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                ),
                BigDecimal.valueOf(320),
                true,
                List.of("Configuración dentro del presupuesto."),
                new CompatibilityResult(true, List.of(), 105, 132)
        );

        when(quotationService.createQuotation(any())).thenReturn(response);

        mockMvc.perform(post("/api/quotations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usageType":"gaming",
                                  "budget":1000,
                                  "components":[
                                    {
                                      "category":"CPU",
                                      "model":"AMD Ryzen 7",
                                      "price":320,
                                      "socket":"AM5",
                                      "powerConsumptionWatts":105
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usageType").value("gaming"))
                .andExpect(jsonPath("$.totalPrice").value(320))
                .andExpect(jsonPath("$.withinBudget").value(true))
                .andExpect(jsonPath("$.compatibility.compatible").value(true));
    }

    @Test
    void recommendationEndpointShouldReturnComponents() throws Exception {

        when(recommendationProcessService.recommend(
                "gaming",
                BigDecimal.valueOf(2000)
        )).thenReturn(List.of(
                new ComponentDto(
                        "CPU",
                        "AMD Ryzen 7",
                        BigDecimal.valueOf(320),
                        "AM5",
                        null,
                        null,
                        105,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        ));

        mockMvc.perform(get("/api/quotations/recommendation")
                        .param("usage", "gaming")
                        .param("budget", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("CPU"))
                .andExpect(jsonPath("$[0].model").value("AMD Ryzen 7"));
    }

    @Test
    void pdfEndpointShouldReturnDownloadablePdf() throws Exception {

        QuotationResponse response = new QuotationResponse(
                "gaming",
                LocalDateTime.of(2026, 5, 17, 14, 30),
                List.of(
                        new ComponentDto(
                                "CPU",
                                "AMD Ryzen 7",
                                BigDecimal.valueOf(320),
                                "AM5",
                                null,
                                null,
                                105,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        )
                ),
                BigDecimal.valueOf(320),
                true,
                List.of("Configuración dentro del presupuesto."),
                new CompatibilityResult(true, List.of(), 105, 132)
        );

        when(quotationService.createQuotation(any())).thenReturn(response);

        when(pdfService.generateQuotationPdf(response))
                .thenReturn(new byte[]{'%', 'P', 'D', 'F', '-', '1', '.', '4'});

        mockMvc.perform(post("/api/quotations/pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usageType":"gaming",
                                  "budget":1000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(
                        "Content-Disposition",
                        containsString(".pdf")
                ))
                .andExpect(content().bytes(
                        new byte[]{'%', 'P', 'D', 'F', '-', '1', '.', '4'}
                ));
    }

    @Test
    void invalidQuotationRequestShouldReturnBadRequest() throws Exception {

        mockMvc.perform(post("/api/quotations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usageType":"",
                                  "budget":-1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}