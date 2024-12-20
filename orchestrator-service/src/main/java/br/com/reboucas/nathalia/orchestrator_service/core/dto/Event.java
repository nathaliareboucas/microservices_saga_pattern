package br.com.reboucas.nathalia.orchestrator_service.core.dto;

import br.com.reboucas.nathalia.orchestrator_service.core.enums.EEventSource;
import br.com.reboucas.nathalia.orchestrator_service.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private EEventSource source;
    private ESagaStatus status;
    private List<History> eventHistory;
    private LocalDateTime createdAt;

    public void handleEventStatus(ESagaStatus sagaStatus, String message, EEventSource source) {
        this.status = sagaStatus;
        this.source = source;
        this.addToHistory(message, source);
    }

    private void addToHistory(String message, EEventSource source) {
        if (isEmpty(eventHistory)) {
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
