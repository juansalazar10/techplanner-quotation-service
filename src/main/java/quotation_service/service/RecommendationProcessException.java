package quotation_service.service;

public class RecommendationProcessException extends RuntimeException {

    public RecommendationProcessException(String message) {
        super(message);
    }

    public RecommendationProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}