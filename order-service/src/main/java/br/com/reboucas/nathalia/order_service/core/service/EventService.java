package br.com.reboucas.nathalia.order_service.core.service;

import br.com.reboucas.nathalia.order_service.config.exception.ValidationException;
import br.com.reboucas.nathalia.order_service.core.document.Event;
import br.com.reboucas.nathalia.order_service.core.document.Order;
import br.com.reboucas.nathalia.order_service.core.dto.EventFilters;
import br.com.reboucas.nathalia.order_service.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
@AllArgsConstructor
@Slf4j
public class EventService {
    private static final String CURRENT_SOURCE = "ORDER_SERVICE";
    private final EventRepository repository;

    public Event createPayload(Order order) {
        var event = Event.builder()
                .orderId(order.getId())
                .transactionId(order.getTransactionId())
                .payload(order)
                .createdAt(LocalDateTime.now())
                .build();
        event.handleEventStatus("Order created!", CURRENT_SOURCE);
        repository.save(event);
        return event;
    }

    public void notifyEnding(Event event) {
        event.setCreatedAt(LocalDateTime.now());
        repository.save(event);
        log.info("Order {} with saga notified! transactionId: {}", event.getOrderId(), event.getTransactionId());
    }

    public List<Event> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFilters eventFilters) {
        eventFilters.validate();
        if (!isEmpty(eventFilters.getOrderId())) {
            return findByOrderId(eventFilters.getOrderId());
        }

        return findByTransactionId(eventFilters.getTransactionId());
    }

    public Event findByOrderId(String orderId) {
        return repository.findTop1ByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ValidationException("Event not found by orderId."));
    }

    public Event findByTransactionId(String transactionId) {
        return repository.findTop1ByTransactionIdOrderByCreatedAtDesc(transactionId)
                .orElseThrow(() -> new ValidationException("Event not found by transactionId."));
    }
}
