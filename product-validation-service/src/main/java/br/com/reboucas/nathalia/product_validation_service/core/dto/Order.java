package br.com.reboucas.nathalia.product_validation_service.core.dto;

import br.com.reboucas.nathalia.product_validation_service.config.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    private String id;
    private List<OrderProducts> products;
    private LocalDateTime createdAt;
    private String transactionId;
    private double totalAmount;
    private int totalItems;

    public void validate() {
        if (isEmpty(id) || isEmpty(transactionId)) {
            throw new ValidationException("OrderId and TransactionId must be informed!");
        }
        if (isEmpty(products)) {
            throw new ValidationException("Product list is empty!");
        }
        products.forEach(OrderProducts::validate);
    }
}
