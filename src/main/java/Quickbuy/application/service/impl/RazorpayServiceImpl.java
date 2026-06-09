package Quickbuy.application.service.impl;

import Quickbuy.application.service.RazorpayService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class RazorpayServiceImpl implements RazorpayService {

    @Value("${razorpay.api.key}")
    private String keyId;

    @Value("${rozarpay.api.secret}")
    private String keySecret;

    @Value("${app.currency}")
    private String currency;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void initializeClient(){
        try {

            System.out.println(">>> Razorpay Key ID being used: [" + keyId + "]");
            System.out.println(">>> Razorpay Secret being used: [" + keySecret + "]");

            this.razorpayClient = new RazorpayClient(keyId, keySecret);

            System.out.println(">>> Razorpay Key ID being used: [" + keyId + "]");
            System.out.println(">>> Razorpay Secret being used: [" + keySecret + "]");
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to initialize Razorpay client", e);
        }
    }


    public Order createOrder(long amountInPaise, String receipt)
            throws RazorpayException {

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);

        return razorpayClient.orders.create(orderRequest);
    }

    public String getKeyId() {
        return keyId;
    }

    @Override
    public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            // Razorpay's rule:
            // payload = orderId + "|" + paymentId
            // expected = HMAC-SHA256(payload, your_secret_key)
            // if expected == razorpaySignature → payment is genuine
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            String expected = hmacSha256(payload, keySecret);
            return expected.equals(razorpaySignature);
        } catch (Exception e) {
            return false;
        }
    }

    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"
        );
        mac.init(secretKey);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // convert bytes to hex string
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
