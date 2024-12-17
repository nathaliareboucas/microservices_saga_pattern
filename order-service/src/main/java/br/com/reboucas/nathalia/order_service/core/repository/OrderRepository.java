package br.com.reboucas.nathalia.order_service.core.repository;

import br.com.reboucas.nathalia.order_service.core.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
}
