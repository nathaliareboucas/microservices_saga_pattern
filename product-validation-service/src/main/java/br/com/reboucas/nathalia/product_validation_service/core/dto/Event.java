package br.com.reboucas.nathalia.product_validation_service.core.dto;

import br.com.reboucas.nathalia.product_validation_service.config.exception.ValidationException;
import br.com.reboucas.nathalia.product_validation_service.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    private String id;
    private String transactionId;
    private String orderId;
    private Order payload;
    private String source;
    private ESagaStatus status;
    private List<History> eventHistory;
    private LocalDateTime createdAt;

    public void validate() {
        if (isEmpty(payload)) {
            throw new ValidationException("Order must be informed!");
        }
        payload.validate();
    }

    public void handleEventStatus(ESagaStatus status, String message, String source) {
        this.status = status;
        this.source = source;
        this.addToHistory(message, source);
    }

    private void addToHistory(String message, String source) {
        if (ObjectUtils.isEmpty(eventHistory)) {
            eventHistory = new ArrayList<>();
        }
        eventHistory.add(History.builder()
                .source(source)
                .status(this.status)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build());
    }

}
