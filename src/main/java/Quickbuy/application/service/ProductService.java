package Quickbuy.application.service;

import Quickbuy.application.dto.ProductListResponse;

import java.util.List;

public interface ProductService {

    public List<ProductListResponse> getAllProducts();
}
