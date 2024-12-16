package br.com.reboucas.nathalia.orchestrator_service.core.dto;

import br.com.reboucas.nathalia.orchestrator_service.core.enums.EEventSource;
import br.com.reboucas.nathalia.orchestrator_service.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
}