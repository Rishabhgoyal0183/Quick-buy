package Quickbuy.application.service.impl;

import Quickbuy.application.dto.PaymentFailureRequest;
import Quickbuy.application.dto.PaymentVerifyRequest;
import Quickbuy.application.dto.VerifyPaymentResponse;
import Quickbuy.application.entity.Order;
import Quickbuy.application.entity.Payment;
import Quickbuy.application.repository.OrderRepository;
import Quickbuy.application.repository.PaymentRepository;
import Quickbuy.application.service.PaymentService;
import Quickbuy.application.service.RazorpayService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayService razorpayService;

    private final OrderRepository orderRepository;

    private final PaymentRepository paymentRepository;

    @Override
    public VerifyPaymentResponse verifyPayment(PaymentVerifyRequest paymentVerifyRequest) {
        log.info("Verifying payment for orderId: {}, paymentId: {}",
                paymentVerifyRequest.getRazorpayOrderId(),
                paymentVerifyRequest.getRazorpayPaymentId());

        Order order = orderRepository.findByRazorpayOrderId(paymentVerifyRequest.getRazorpayOrderId())
                .orElseThrow(() -> {
                    log.error("No order found in database for Razorpay Order ID: {}",
                            paymentVerifyRequest.getRazorpayOrderId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid Razorpay Order ID: " + paymentVerifyRequest.getRazorpayOrderId());
                });

        if (order.getStatus() == Order.Status.PAID) {
            log.info("Order {} already PAID — skipping duplicate verify", order.getId());
            return new VerifyPaymentResponse("SUCCESS",
                    "Payment already verified for this order");
        }

        boolean isVerified = razorpayService.verifySignature(paymentVerifyRequest.getRazorpayOrderId(),
                paymentVerifyRequest.getRazorpayPaymentId(),
                paymentVerifyRequest.getRazorpaySignature()
        );

        if (!isVerified){
            log.warn("Payment verification failed for orderId: {}, paymentId: {}",
                    paymentVerifyRequest.getRazorpayOrderId(),
                    paymentVerifyRequest.getRazorpayPaymentId());

            savePayment(paymentVerifyRequest, order, Payment.Status.FAILED);
            updateOrderStatus(order, Order.Status.FAILED);
            return new VerifyPaymentResponse("FAILED", "Payment verification failed");
        }
        log.info("Payment verification successful for orderId: {}, paymentId: {}",
                paymentVerifyRequest.getRazorpayOrderId(),
                paymentVerifyRequest.getRazorpayPaymentId());

        savePayment(paymentVerifyRequest, order, Payment.Status.SUCCESS);
        updateOrderStatus(order, Order.Status.PAID);
        return new VerifyPaymentResponse("SUCCESS", "Payment verified successfully");

    }

    @Override
    @Transactional
    public String markPaymentFailed(PaymentFailureRequest request) {

        // Find order by razorpayOrderId
        Order order = orderRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> {
                    log.error("No order found for razorpayOrderId: {}",
                            request.getRazorpayOrderId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Order not found: " + request.getRazorpayOrderId());
                });

        // Guard — don't overwrite if already PAID
        // (edge case: success and failure events arrive out of order)
        if (order.getStatus() == Order.Status.PAID) {
            log.warn("Skipping failure update — order {} is already PAID", order.getId());
            throw new IllegalArgumentException(
                    "Order already marked as PAID, skipping failure update");
        }

        if (Objects.equals(order.getRazorpayOrderId(), request.getRazorpayOrderId())) {
            log.warn("Skipping failure update — order {} is already marked as FAILED", order.getId());
            throw new IllegalArgumentException(
                    "Order already marked as FAILED, skipping failure update");
        }

        // Update order status → FAILED
        order.setStatus(Order.Status.FAILED);
        orderRepository.save(order);
        log.info("Order {} marked as FAILED", order.getId());

        // Save Payment record with FAILED status
        // No signature available on failure — that's fine, just save what we have
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(null); // no signature on failure
        payment.setStatus(Payment.Status.FAILED);
        paymentRepository.save(payment);
        log.info("Payment failure record saved for order {}", order.getId());
        return "Payment marked as FAILED for order " + order.getId();
    }

    private void updateOrderStatus(Order order, Order.Status status) {

        order.setStatus(status);
        orderRepository.save(order);
        log.info("Updated order status to {} for orderId: {}", status, order.getId());
    }

    private void savePayment(PaymentVerifyRequest paymentVerifyRequest, Order order, Payment.Status status) {
        Payment payment = new Payment();
        payment.setRazorpayPaymentId(paymentVerifyRequest.getRazorpayPaymentId());
        payment.setRazorpaySignature(paymentVerifyRequest.getRazorpaySignature());
        payment.setOrder(order);
        payment.setStatus(status);
        payment.setPaidAt(LocalDateTime.now());

        paymentRepository.save(payment);
    }
}
