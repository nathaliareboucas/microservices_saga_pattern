package br.com.reboucas.nathalia.order_service.core.service;

import br.com.reboucas.nathalia.order_service.core.document.Event;
import br.com.reboucas.nathalia.order_service.core.document.Order;
import br.com.reboucas.nathalia.order_service.core.dto.OrderRequest;
import br.com.reboucas.nathalia.order_service.core.producer.SagaProducer;
import br.com.reboucas.nathalia.order_service.core.repository.OrderRepository;
import br.com.reboucas.nathalia.order_service.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {
    private static final String TRANSACTION_ID_PATTERN = "%S_%S";

    private final OrderRepository orderRepository;
    private final JsonUtil jsonUtil;
    private final SagaProducer sagaProducer;
    private final EventService eventService;

    public Order create(OrderRequest orderRequest) {
        var order = Order.builder()
                .products(orderRequest.getProducts())
                .createdAt(LocalDateTime.now())
                .transactionId(
                        String.format(TRANSACTION_ID_PATTERN,
                        Instant.now().toEpochMilli(),
                        UUID.randomUUID())
                )
                .build();
        orderRepository.save(order);
        emitEvent(order);
        return order;
    }

    private void emitEvent(Order orderCreated) {
        var payload = eventService.createPayload(orderCreated);
        sagaProducer.sendEvent(jsonUtil.toJson(payload));
    }

}
