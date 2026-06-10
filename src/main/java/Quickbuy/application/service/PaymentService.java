package Quickbuy.application.service;

import Quickbuy.application.dto.PaymentFailureRequest;
import Quickbuy.application.dto.PaymentVerifyRequest;
import Quickbuy.application.dto.VerifyPaymentResponse;

public interface PaymentService {

    VerifyPaymentResponse verifyPayment(PaymentVerifyRequest paymentVerifyRequest);

    String markPaymentFailed(PaymentFailureRequest request);
}
