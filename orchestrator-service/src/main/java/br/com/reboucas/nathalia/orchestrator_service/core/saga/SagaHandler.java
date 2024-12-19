package br.com.reboucas.nathalia.orchestrator_service.core.saga;

import br.com.reboucas.nathalia.orchestrator_service.config.exception.ValidationException;
import br.com.reboucas.nathalia.orchestrator_service.core.enums.EEventSource;
import br.com.reboucas.nathalia.orchestrator_service.core.enums.ESagaStatus;
import br.com.reboucas.nathalia.orchestrator_service.core.enums.ETopics;

import java.util.Arrays;

import static br.com.reboucas.nathalia.orchestrator_service.core.enums.EEventSource.*;
import static br.com.reboucas.nathalia.orchestrator_service.core.enums.ESagaStatus.*;
import static br.com.reboucas.nathalia.orchestrator_service.core.enums.ETopics.*;

public final class SagaHandler {
    private SagaHandler() {}

    private static final int EVENT_SOURCE_INDEX = 0;
    private static final int SAGA_STATUS_INDEX = 1;
    private static final int TOPIC_INDEX = 2;

    private static final Object[][] SAGA_HANDLER = {
            {ORCHESTRATOR, SUCCESS, PRODUCT_VALIDATION_SUCCESS},
            {ORCHESTRATOR, FAIL, FINISH_FAIL},

            {PRODUCT_VALIDATION_SERVICE, ROLLBACK_PENDING, PRODUCT_VALIDATION_FAIL},
            {PRODUCT_VALIDATION_SERVICE, FAIL, FINISH_FAIL},
            {PRODUCT_VALIDATION_SERVICE, SUCCESS, PAYMENT_SUCCESS},

            {PAYMENT_SERVICE, ROLLBACK_PENDING, PAYMENT_FAIL},
            {PAYMENT_SERVICE, FAIL, PRODUCT_VALIDATION_FAIL},
            {PAYMENT_SERVICE, SUCCESS, INVENTORY_SUCCESS},

            {INVENTORY_SERVICE, ROLLBACK_PENDING, INVENTORY_FAIL},
            {INVENTORY_SERVICE, FAIL, PAYMENT_FAIL},
            {INVENTORY_SERVICE, SUCCESS, FINISH_SUCCESS},
    };

    public static ETopics findTopicBySourceAndStatus(EEventSource sourceEvent, ESagaStatus statusEvent) {
        return (ETopics) (Arrays.stream(SAGA_HANDLER)
                .filter(row -> isEventSourceAndStatusValid(sourceEvent, statusEvent, row))
                .map(i -> i[TOPIC_INDEX])
                .findFirst()
                .orElseThrow(() -> new ValidationException("Topic not found!")));
    }

    private static boolean isEventSourceAndStatusValid(EEventSource sourceEvent, ESagaStatus statusEvent, Object[] row) {
        var source = row[EVENT_SOURCE_INDEX];
        var status = row[SAGA_STATUS_INDEX];
        return sourceEvent.equals(source) && statusEvent.equals(status);
    }
}
