package br.com.reboucas.nathalia.inventory_service.core.model;

import br.com.reboucas.nathalia.inventory_service.config.exception.ValidationException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private int available;

    public void checkAvailability(int orderQuantity) {
        if (orderQuantity > available) {
            throw new ValidationException("Product is out of stock!");
        }
    }

    public void toGoDown(int orderQuantity) {
        available = available - orderQuantity;
    }

    public void returnPreviousValues(int oldQuantity) {
        available = oldQuantity;
    }
}
