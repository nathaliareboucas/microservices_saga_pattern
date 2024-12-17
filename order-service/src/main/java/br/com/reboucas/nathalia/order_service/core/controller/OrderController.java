package br.com.reboucas.nathalia.order_service.core.controller;

import br.com.reboucas.nathalia.order_service.core.document.Order;
import br.com.reboucas.nathalia.order_service.core.dto.OrderRequest;
import br.com.reboucas.nathalia.order_service.core.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody OrderRequest orderRequest) {
        var orderCreated = orderService.create(orderRequest);
        return ResponseEntity.ok(orderCreated);
    }
}
