package br.com.reboucas.nathalia.payment_service.core.service;

import br.com.reboucas.nathalia.payment_service.config.exception.ValidationException;
import br.com.reboucas.nathalia.payment_service.core.dto.Event;
import br.com.reboucas.nathalia.payment_service.core.enums.EPaymentStatus;
import br.com.reboucas.nathalia.payment_service.core.enums.ESagaStatus;
import br.com.reboucas.nathalia.payment_service.core.model.Payment;
import br.com.reboucas.nathalia.payment_service.core.producer.KafkaProducer;
import br.com.reboucas.nathalia.payment_service.core.repository.PaymentRepository;
import br.com.reboucas.nathalia.payment_service.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentService {
    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";

    private final JsonUtil jsonUtil;
    private final KafkaProducer producer;
    private final PaymentRepository paymentRepository;

    public void realizePayment(Event event) {
        try {
            validateExistingOrder(event.getOrderId(), event.getTransactionId());
            savePendingPayment(event);

            // Here would include notification or integration with a payment method
            var payment = findOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId());
            payment.validateTotalAmount();
            updatePayment(EPaymentStatus.SUCCESS, payment);
            event.handleEventStatus(ESagaStatus.SUCCESS,
                    "Payment realized successfully!", CURRENT_SOURCE);
        } catch (Exception e) {
            log.error("Error trying to make payment:", e);
            event.handleEventStatus(ESagaStatus.ROLLBACK_PENDING,
                    "Fail to realize payment!", CURRENT_SOURCE);
        }
        producer.sendEvent(jsonUtil.toJson(event));
    }

    public void realizeRefund(Event event) {
        try {
            Payment payment = findOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId());
            updatePayment(EPaymentStatus.REFUND, payment);
            event.handleEventStatus(ESagaStatus.FAIL,
                    "Rollback executed for payment!", CURRENT_SOURCE);
        } catch (Exception e) {
            log.error("Error trying to make refund:", e);
            event.handleEventStatus(ESagaStatus.ROLLBACK_PENDING,
                    "Rollback not executed for payment: ".concat(e.getMessage()), CURRENT_SOURCE);
        }
        producer.sendEvent(jsonUtil.toJson(event));
    }

    private void updatePayment(EPaymentStatus status, Payment payment) {
        payment.setStatus(status);
        paymentRepository.save(payment);
    }

    private void savePendingPayment(Event event) {
        var payment = Payment.builder()
                .orderId(event.getOrderId())
                .transactionId(event.getTransactionId())
                .totalItems(event.getPayload().getTotalItems())
                .totalAmount(event.getPayload().getTotalAmount())
                .build();
        payment.validateTotalAmount();
        paymentRepository.save(payment);
    }

    private Payment findOrderIdAndTransactionId(String orderId, String transactionId) {
        return paymentRepository.findByOrderIdAndTransactionId(orderId, transactionId)
                .orElseThrow(() -> new ValidationException("Payment not found by orderId and transactionId"));
    }

    private void validateExistingOrder(String orderId, String transactionId) {
        Boolean existingOrder = paymentRepository.existsByOrderIdAndTransactionId(orderId, transactionId);
        if (existingOrder) {
            throw new ValidationException("There's another transactionId for this validation!");
        }
    }
}
