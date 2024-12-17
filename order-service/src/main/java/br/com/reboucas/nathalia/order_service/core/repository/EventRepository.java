package br.com.reboucas.nathalia.order_service.core.repository;

import br.com.reboucas.nathalia.order_service.core.document.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRepository extends MongoRepository<Event, String> {
}
