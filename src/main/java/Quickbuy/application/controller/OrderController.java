package Quickbuy.application.controller;

import Quickbuy.application.dto.CreateOrderRequest;
import Quickbuy.application.dto.CreateOrderResponse;
import Quickbuy.application.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ResponseEntity.badRequest().body("Invalid order request: " + bindingResult.getAllErrors());
        }

        return ResponseEntity.ok(orderService.createOrder(request));
    }
}
