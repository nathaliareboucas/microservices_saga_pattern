package br.com.reboucas.nathalia.inventory_service.core.service;

import br.com.reboucas.nathalia.inventory_service.config.exception.ValidationException;
import br.com.reboucas.nathalia.inventory_service.core.dto.Event;
import br.com.reboucas.nathalia.inventory_service.core.dto.Order;
import br.com.reboucas.nathalia.inventory_service.core.dto.OrderProducts;
import br.com.reboucas.nathalia.inventory_service.core.model.Inventory;
import br.com.reboucas.nathalia.inventory_service.core.model.OrderInventory;
import br.com.reboucas.nathalia.inventory_service.core.producer.KafkaProducer;
import br.com.reboucas.nathalia.inventory_service.core.repository.InventoryRepository;
import br.com.reboucas.nathalia.inventory_service.core.repository.OrderInventoryRepository;
import br.com.reboucas.nathalia.inventory_service.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import static br.com.reboucas.nathalia.inventory_service.core.enums.ESagaStatus.*;

@Service
@AllArgsConstructor
@Slf4j
public class InventoryService {
    private static final String CURRENT_SOURCE = "INVENTORY_SERVICE";

    private final JsonUtil jsonUtil;
    private final KafkaProducer producer;
    private final InventoryRepository inventoryRepository;
    private final OrderInventoryRepository orderInventoryRepository;

    public void updateInventory(Event event) {
        try {
            validateExistingOrder(event.getOrderId(), event.getTransactionId());
            createOrderInventory(event);
            updateInventory(event.getPayload());
            event.handleEventStatus(SUCCESS, "Inventory updated successfully!", CURRENT_SOURCE);
        } catch (Exception e) {
            log.error("Error trying to update inventory: ", e);
            event.handleEventStatus(ROLLBACK_PENDING,
                    "Fail to update inventory! ".concat(e.getMessage()), CURRENT_SOURCE);
        }
        producer.sendEvent(jsonUtil.toJson(event));
    }

    public void rollbackInventory(Event event) {
        try {
            returnInventoryToPreviousValues(event);
            event.handleEventStatus(FAIL, "Rollback executed for inventory!", CURRENT_SOURCE);
        } catch (Exception e) {
            log.error("Error trying to make rollback inventory: ", e);
            event.handleEventStatus(ROLLBACK_PENDING,
                    "Rollback not executed for inventory".concat(e.getMessage()), CURRENT_SOURCE);
        }
        producer.sendEvent(jsonUtil.toJson(event));
    }

    private void returnInventoryToPreviousValues(Event event) {
        orderInventoryRepository.findByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())
                .forEach(orderInventory -> {
                    var inventory = orderInventory.getInventory();
                    inventory.returnPreviousValues(orderInventory.getOldQuantity());
                    inventoryRepository.save(inventory);
                    log.info("Restored inventory for order {} from {} to {}",
                            event.getOrderId(),
                            orderInventory.getNewQuantity(),
                            inventory.getAvailable());
                });
    }

    private void updateInventory(Order order) {
        order.getProducts().forEach(orderProduct -> {
            var inventory = findByProductCode(orderProduct.getProduct().getCode());
            inventory.checkAvailability(orderProduct.getQuantity());
            inventory.toGoDown(orderProduct.getQuantity());
            inventoryRepository.save(inventory);
        });
    }

    private void createOrderInventory(Event event) {
        event.getPayload().getProducts()
                .forEach(orderProduct -> {
                    var inventory = findByProductCode(orderProduct.getProduct().getCode());
                    var orderInventory = buildOrderInventory(event, orderProduct, inventory);
                    orderInventoryRepository.save(orderInventory);
                });
    }

    private OrderInventory buildOrderInventory(Event event, OrderProducts orderProducts, Inventory inventory) {
        return OrderInventory.builder()
                .inventory(inventory)
                .oldQuantity(inventory.getAvailable())
                .orderQuantity(orderProducts.getQuantity())
                .newQuantity(inventory.getAvailable() - orderProducts.getQuantity())
                .orderId(event.getOrderId())
                .transactionId(event.getTransactionId())
                .build();
    }

    private Inventory findByProductCode(String productCode) {
        return inventoryRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ValidationException("Inventory not found by product code!"));
    }

    private void validateExistingOrder(String orderId, String transactionId) {
        Boolean existingOrder = orderInventoryRepository.existsByOrderIdAndTransactionId(orderId, transactionId);
        if (existingOrder) {
            throw new ValidationException("There's another transactionId for this validation!");
        }
    }
}
