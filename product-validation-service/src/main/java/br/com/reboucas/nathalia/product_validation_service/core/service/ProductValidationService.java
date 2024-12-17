package br.com.reboucas.nathalia.product_validation_service.core.service;

import br.com.reboucas.nathalia.product_validation_service.config.exception.ValidationException;
import br.com.reboucas.nathalia.product_validation_service.core.dto.Event;
import br.com.reboucas.nathalia.product_validation_service.core.dto.OrderProducts;
import br.com.reboucas.nathalia.product_validation_service.core.model.Validation;
import br.com.reboucas.nathalia.product_validation_service.core.producer.KafkaProducer;
import br.com.reboucas.nathalia.product_validation_service.core.repository.ProductRepository;
import br.com.reboucas.nathalia.product_validation_service.core.repository.ValidationRepository;
import br.com.reboucas.nathalia.product_validation_service.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static br.com.reboucas.nathalia.product_validation_service.core.enums.ESagaStatus.*;

@Service
@AllArgsConstructor
@Slf4j
public class ProductValidationService {
    private static final String CURRENT_SOURCE = "PRODUCT_VALIDATION_SERVICE";

    private final JsonUtil jsonUtil;
    private final KafkaProducer producer;
    private final ValidationRepository validationRepository;
    private final ProductRepository productRepository;

    public void validateExistingProducts(Event event) {
        try {
            event.validate();
            validateExistingOrder(event.getOrderId(), event.getTransactionId());
            validateExistingProducts(event.getPayload().getProducts());
            saveValidation(event, true);
            event.handleEventStatus(SUCCESS,
                    "Products are validated successfully!", CURRENT_SOURCE);
        } catch (Exception e) {
            log.error("Error trying to validate products:", e);
            event.handleEventStatus(ROLLBACK_PENDING,
                    "Fail to validate products: " + e.getMessage(), CURRENT_SOURCE);
        }
        producer.sendEvent(jsonUtil.toJson(event));
    }

    public void rollbackEvent(Event event) {
        changeValidtionToFail(event);
        event.handleEventStatus(FAIL, "Rollback executed on product validation!", CURRENT_SOURCE);
        producer.sendEvent(jsonUtil.toJson(event));
    }

    private void changeValidtionToFail(Event event) {
        validationRepository.findByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())
                .ifPresentOrElse(validation -> {
                    validation.setSuccess(false);
                    validationRepository.save(validation);
                }, () -> saveValidation(event, false));
    }

    private void validateExistingOrder(String orderId, String transactionId) {
        Boolean existingOrder = validationRepository.existsByOrderIdAndTransactionId(orderId, transactionId);
        if (existingOrder) {
            throw new ValidationException("There's another transactionId for this validation!");
        }
    }

    private void validateExistingProducts(List<OrderProducts> orderProducts) {
        orderProducts.forEach(orderProduct -> {
            var productCode = orderProduct.getProduct().getCode();
            if (!productRepository.existsByCode(productCode)) {
                throw new ValidationException("Product with code " + productCode + " does not exists!");
            }
        });
    }

    private void saveValidation(Event event, boolean success) {
        var validation = Validation.builder()
                .orderId(event.getOrderId())
                .transactionId(event.getTransactionId())
                .success(success)
                .build();
        validationRepository.save(validation);
    }
}