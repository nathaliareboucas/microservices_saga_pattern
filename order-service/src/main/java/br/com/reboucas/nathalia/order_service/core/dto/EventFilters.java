package br.com.reboucas.nathalia.order_service.core.dto;

import br.com.reboucas.nathalia.order_service.config.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.springframework.util.ObjectUtils.isEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFilters {
    private String orderId;
    private String transactionId;

    public void validate() {
        if (isEmpty(this.orderId) && isEmpty(this.transactionId)) {
            throw new ValidationException("OrderId or TransactionId must be informed.");
        }
    }
}
