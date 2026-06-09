package Quickbuy.application.service.impl;

import Quickbuy.application.dto.PaymentVerifyRequest;
import Quickbuy.application.dto.VerifyPaymentResponse;
import Quickbuy.application.entity.Order;
import Quickbuy.application.entity.Payment;
import Quickbuy.application.repository.OrderRepository;
import Quickbuy.application.repository.PaymentRepository;
import Quickbuy.application.service.PaymentService;
import Quickbuy.application.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
                    return new IllegalArgumentException("Invalid Razorpay Order ID: " + paymentVerifyRequest.getRazorpayOrderId());
                });

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
