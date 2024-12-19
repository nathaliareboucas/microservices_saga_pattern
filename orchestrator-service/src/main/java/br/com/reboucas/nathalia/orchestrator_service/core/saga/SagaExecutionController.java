package br.com.reboucas.nathalia.orchestrator_service.core.saga;

import br.com.reboucas.nathalia.orchestrator_service.config.exception.ValidationException;
import br.com.reboucas.nathalia.orchestrator_service.core.dto.Event;
import br.com.reboucas.nathalia.orchestrator_service.core.enums.ETopics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static br.com.reboucas.nathalia.orchestrator_service.core.saga.SagaHandler.*;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
@AllArgsConstructor
@Slf4j
public class SagaExecutionController {
    private static final String SAGA_LOG_ID = "OrderId: %s | TransactionId: %s | EventId: %s";

    public ETopics getNextTopic(Event event) {
        if (isEmpty(event.getSource()) || isEmpty(event.getStatus())) {
            throw new ValidationException("Source and status must be informed!");
        }
        var topic = findTopicBySourceAndStatus(event.getSource(), event.getStatus());
        logCurrentSaga(event, topic);
        return topic;
    }

    private void logCurrentSaga(Event event, ETopics topic) {
        var sagaId = createSagaId(event);
        var source = event.getSource();
        switch (event.getStatus()) {
            case SUCCESS -> log.info("### CURRENT_SAGA: {} | SUCCESS | NEXT_TOPIC: {} | {}",
                    source, topic, sagaId);
            case ROLLBACK_PENDING -> log.info("### CURRENT_SAGA: {} | SENDING TO ROLLBACK CURRENT SERVICE | NEXT_TOPIC: {} | {}",
                    source, topic, sagaId);
            case FAIL -> log.info("### CURRENT_SAGA: {} | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT_TOPIC: {} | {}",
                    source, topic, sagaId);
        }
    }

    private String createSagaId(Event event) {
        return String.format(SAGA_LOG_ID,
                event.getOrderId(),
                event.getTransactionId(),
                event.getId());
    }
}