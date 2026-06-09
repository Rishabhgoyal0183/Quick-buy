package Quickbuy.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {

    private Long orderId;
    private String razorpayOrderId;
    private Long amount;
    private String currency;
    private String keyId;
    private String productName;
    private String customerName;
    private String customerEmail;
}
