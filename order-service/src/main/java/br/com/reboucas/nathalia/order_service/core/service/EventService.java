package br.com.reboucas.nathalia.order_service.core.service;

import br.com.reboucas.nathalia.order_service.core.document.Event;
import br.com.reboucas.nathalia.order_service.core.document.Order;
import br.com.reboucas.nathalia.order_service.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class EventService {
    private final EventRepository repository;

    public Event save(Event event) {
        return repository.save(event);
    }

    public Event createPayload(Order order) {
        var event = Event.builder()
                .orderId(order.getId())
                .transactionId(order.getTransactionId())
                .payload(order)
                .createdAt(LocalDateTime.now())
                .build();
        save(event);
        return event;
    }
}
