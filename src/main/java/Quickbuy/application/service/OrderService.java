package Quickbuy.application.service;

import Quickbuy.application.dto.CreateOrderRequest;
import Quickbuy.application.dto.CreateOrderResponse;

public interface OrderService {

    CreateOrderResponse createOrder(CreateOrderRequest request);
}
