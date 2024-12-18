package br.com.reboucas.nathalia.order_service.core.document;

import br.com.reboucas.nathalia.order_service.config.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.springframework.util.ObjectUtils.isEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderProducts {
    private Product product;
    private int quantity;

    public void validate() {
        if (isEmpty(product) || isEmpty(product.getCode())) {
            throw new ValidationException("Product must be informed!");
        }
    }
}
