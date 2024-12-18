package br.com.reboucas.nathalia.order_service.core.document;

import br.com.reboucas.nathalia.order_service.config.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "order")
public class Order {

    @Id
    private String id;
    private List<OrderProducts> products;
    private LocalDateTime createdAt;
    private String transactionId;
    private double totalAmount;
    private int totalItems;

    public void validateProducts() {
        if (isEmpty(products)) {
            throw new ValidationException("Product list is empty!");
        }
        products.forEach(OrderProducts::validate);
    }

    public void calculateTotals() {
        this.totalAmount = calculateAmount();
        this.totalItems = calculateTotalItems();
    }

    private double calculateAmount() {
        return products.stream()
                .map(orderProduct -> orderProduct.getQuantity() *
                        orderProduct.getProduct().getUnitValue())
                .reduce(0.0, Double::sum);
    }

    private int calculateTotalItems() {
        return products.stream()
                .map(OrderProducts::getQuantity)
                .reduce(0, Integer::sum);
    }
}
