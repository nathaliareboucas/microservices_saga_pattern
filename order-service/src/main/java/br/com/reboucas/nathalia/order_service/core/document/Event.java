package br.com.reboucas.nathalia.order_service.core.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "event")
public class Event {

    @Id
    private String id;
    private String transactionId;
    private String orderId;
    private Order payload;
    private String source;
    private String status;
    private List<History> eventHistory;
    private LocalDateTime createdAt;

    public void handleEventStatus(String message, String source) {
        this.status = "SUCCESS";
        this.source = source;
        this.addToHistory(message, source);
    }

    private void addToHistory(String message, String source) {
        if (isEmpty(eventHistory)) {
            eventHistory = new ArrayList<>();
        }
        eventHistory.add(History.builder()
                .source(source)
                .status(this.status)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
