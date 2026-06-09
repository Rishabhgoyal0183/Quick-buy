package Quickbuy.application.service;

import com.razorpay.Order;
import com.razorpay.RazorpayException;

public interface RazorpayService {

    Order createOrder(long amountInPaise, String receipt) throws RazorpayException;

    String getKeyId();

    boolean verifySignature(String razorpayOrderId,
                            String razorpayPaymentId,
                            String razorpaySignature);

}
