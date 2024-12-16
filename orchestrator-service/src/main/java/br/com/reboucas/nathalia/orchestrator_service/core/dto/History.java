package br.com.reboucas.nathalia.orchestrator_service.core.dto;

import br.com.reboucas.nathalia.orchestrator_service.core.enums.EEventSource;
import br.com.reboucas.nathalia.orchestrator_service.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class History {
    private EEventSource source;
    private ESagaStatus status;
    private String message;
    private LocalDateTime createdAt;
}
