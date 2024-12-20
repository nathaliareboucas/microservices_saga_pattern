package br.com.reboucas.nathalia.orchestrator_service.core.service;

import br.com.reboucas.nathalia.orchestrator_service.core.dto.Event;
import br.com.reboucas.nathalia.orchestrator_service.core.producer.SagaOrchestratorProducer;
import br.com.reboucas.nathalia.orchestrator_service.core.saga.SagaExecutionController;
import br.com.reboucas.nathalia.orchestrator_service.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static br.com.reboucas.nathalia.orchestrator_service.core.enums.EEventSource.ORCHESTRATOR;
import static br.com.reboucas.nathalia.orchestrator_service.core.enums.ESagaStatus.FAIL;
import static br.com.reboucas.nathalia.orchestrator_service.core.enums.ESagaStatus.SUCCESS;
import static br.com.reboucas.nathalia.orchestrator_service.core.enums.ETopics.BASE_ORCHESTRATOR;
import static br.com.reboucas.nathalia.orchestrator_service.core.enums.ETopics.NOTIFY_ENDING;

@Service
@AllArgsConstructor
@Slf4j
public class OrchestratorService {
    private final JsonUtil jsonUtil;
    private final SagaOrchestratorProducer producer;
    private final SagaExecutionController sagaExecutionController;

    public void startSaga(Event event) {
        log.info("SAGA STARTED!");
        event.handleEventStatus(SUCCESS, "SAGA started!", ORCHESTRATOR);
        producer.sendEvent(BASE_ORCHESTRATOR.getTopic(), jsonUtil.toJson(event));
    }

    public void finishSagaSuccess(Event event) {
        log.info("SAGA FINISHED SUCCESSFULLY FOR EVENT {}!", event.getId());
        event.handleEventStatus(SUCCESS, "SAGA finished successfully!", ORCHESTRATOR);
        producer.sendEvent(NOTIFY_ENDING.getTopic(), jsonUtil.toJson(event));
    }

    public void finishSagaFail(Event event) {
        log.info("SAGA FINISHED WITH ERRORS FOR EVENT {}!", event.getId());
        event.handleEventStatus(FAIL, "SAGA finished with errors!", ORCHESTRATOR);
        producer.sendEvent(NOTIFY_ENDING.getTopic(), jsonUtil.toJson(event));
    }

    public void continueSaga(Event event) {
        var topic = sagaExecutionController.getNextTopic(event);
        log.info("SAGA CONTINUING FOR EVENT {}!", event.getId());
        event.handleEventStatus(SUCCESS, "SAGA continuing!", ORCHESTRATOR);
        producer.sendEvent(topic.getTopic(), jsonUtil.toJson(event));
    }
}