package br.com.reboucas.nathalia.order_service.core.controller;

import br.com.reboucas.nathalia.order_service.core.document.Event;
import br.com.reboucas.nathalia.order_service.core.dto.EventFilters;
import br.com.reboucas.nathalia.order_service.core.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/event")
public class EventController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<Event> findByFilters(@RequestBody EventFilters eventFilters) {
        Event existingEvent = eventService.findByFilters(eventFilters);
        return ResponseEntity.ok(existingEvent);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Event>> findAll() {
        List<Event> events = eventService.findAll();
        return ResponseEntity.ok(events);
    }
}
