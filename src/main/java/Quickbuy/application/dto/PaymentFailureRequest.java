package Quickbuy.application.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentFailureRequest {

    @NotBlank(message = "Razorpay Order ID is required")
    private String razorpayOrderId;   // from response.error.metadata.order_id

    private String razorpayPaymentId; // from response.error.metadata.payment_id

    private String failureReason;     // from response.error.description
}
