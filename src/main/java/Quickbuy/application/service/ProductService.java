package Quickbuy.application.service;

import Quickbuy.application.dto.ProductListResponse;
import Quickbuy.application.dto.SaveUpdateProductRequest;

import java.util.List;

public interface ProductService {

    List<ProductListResponse> getAllProducts();

    String saveProduct(SaveUpdateProductRequest saveUpdateProductRequest);

    String updateProduct(SaveUpdateProductRequest saveUpdateProductRequest, Long id);
}
