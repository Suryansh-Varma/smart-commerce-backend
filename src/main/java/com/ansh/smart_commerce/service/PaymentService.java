package com.ansh.smart_commerce.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ansh.smart_commerce.dto.PaymentResponse;
import com.ansh.smart_commerce.entity.Order;
import com.ansh.smart_commerce.entity.Payment;
import com.ansh.smart_commerce.enums.PaymentMethod;
import com.ansh.smart_commerce.enums.PaymentStatus;
import com.ansh.smart_commerce.exception.OrderNotFoundException;
import com.ansh.smart_commerce.exception.PaymentFailedException;
import com.ansh.smart_commerce.repository.OrderRepository;
import com.ansh.smart_commerce.repository.PaymentRepository;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public PaymentResponse createPayment(Long orderId, PaymentMethod paymentMethod, double amount) {
        log.info("Creating payment for order {} via {}", orderId, paymentMethod);
        Order order = resolveOrder(orderId);

        PaymentStatus initialStatus = (paymentMethod == PaymentMethod.COD)
                ? PaymentStatus.PENDING
                : PaymentStatus.PENDING;

        String transactionId = UUID.randomUUID().toString();

        Payment payment = new Payment(order, paymentMethod, initialStatus, transactionId, amount, LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        log.info("Payment {} created for order {} — status: {}", saved.getId(), orderId, initialStatus);
        return PaymentResponse.from(saved);
    }

    @Transactional
    public PaymentResponse markSuccess(Long paymentId) {
        log.info("Marking payment {} as SUCCESS", paymentId);
        Payment payment = resolvePayment(paymentId);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        Payment saved = paymentRepository.save(payment);
        log.info("Payment {} marked SUCCESS", paymentId);
        return PaymentResponse.from(saved);
    }

    @Transactional
    public PaymentResponse markFailed(Long paymentId) {
        log.warn("Marking payment {} as FAILED", paymentId);
        Payment payment = resolvePayment(paymentId);
        payment.setPaymentStatus(PaymentStatus.FAILED);
        Payment saved = paymentRepository.save(payment);
        log.warn("Payment {} marked FAILED", paymentId);
        return PaymentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        return PaymentResponse.from(resolvePayment(paymentId));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrder(Long orderId) {
        log.info("Fetching payment for order {}", orderId);
        Order order = resolveOrder(orderId);
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new PaymentFailedException("No payment found for order id: " + orderId));
        return PaymentResponse.from(payment);
    }

    private Payment resolvePayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("Payment not found: {}", paymentId);
                    return new PaymentFailedException("Payment not found with id: " + paymentId);
                });
    }

    private Order resolveOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
