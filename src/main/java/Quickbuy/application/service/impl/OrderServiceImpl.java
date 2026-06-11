package Quickbuy.application.service.impl;

import Quickbuy.application.dto.CreateOrderRequest;
import Quickbuy.application.dto.CreateOrderResponse;
import Quickbuy.application.entity.Order;
import Quickbuy.application.entity.Product;
import Quickbuy.application.repository.OrderRepository;
import Quickbuy.application.repository.ProductRepository;
import Quickbuy.application.service.OrderService;
import com.razorpay.RazorpayException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Repository
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final RazorpayServiceImpl razorpayServiceImpl;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {

        // Step 1: Validate product exists in DB
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found with id: " + request.getProductId()
                ));

        if (product.getPrice() == null ||
                product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid product price, cannot create order"
            );
        }
        long amountInPaise = product.getPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        // Step 2: Create order on Razorpay's server
        // receipt is just a unique label for your reference — not shown to user
        com.razorpay.Order razorpayOrder;
        try {
            razorpayOrder = razorpayServiceImpl.createOrder(
                    amountInPaise,
                    "receipt_" + System.currentTimeMillis()
            );
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to initiate payment. Please try again."
            );
        }

        String razorpayOrderId = razorpayOrder.get("id"); // looks like: order_Pxxxxxx
        log.info("Razorpay order created: {}", razorpayOrderId);

        Order order = saveOrderAndGet(request, product, razorpayOrderId);

        return new CreateOrderResponse(
                order.getId(),
                razorpayOrderId,
                amountInPaise,
                "INR",
                razorpayServiceImpl.getKeyId(),
                product.getName(),
                request.getCustomerName(),
                request.getCustomerEmail()
        );
    }

    private Order saveOrderAndGet(CreateOrderRequest request, Product product, String razorpayOrderId) {
        // Save our own Order row in DB with PENDING status
        Order order = new Order();
        order.setProduct(product);
        order.setRazorpayOrderId(razorpayOrderId);
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setAmount(product.getPrice());
        order.setStatus(Order.Status.PENDING);
        order = orderRepository.save(order);
        return order;
    }
}
