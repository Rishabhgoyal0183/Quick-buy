package Quickbuy.application.service.impl;

import Quickbuy.application.dto.ProductListResponse;
import Quickbuy.application.entity.Product;
import Quickbuy.application.repository.ProductRepository;
import Quickbuy.application.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    public final ProductRepository productRepository;

    public List<ProductListResponse> getAllProducts() {
        // Implement logic to fetch products from the database
        return productRepository.findAll().stream().map(product -> {
            ProductListResponse response = new ProductListResponse();
            response.setId(product.getId());
            response.setName(product.getName());
            response.setDescription(product.getDescription());
            response.setPrice(product.getPrice());
            response.setImageUrl(product.getImageUrl());
            return response;
        }).toList();
    }


}
