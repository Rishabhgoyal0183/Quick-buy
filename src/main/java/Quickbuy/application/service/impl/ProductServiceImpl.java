package Quickbuy.application.service.impl;

import Quickbuy.application.dto.ProductListResponse;
import Quickbuy.application.dto.SaveUpdateProductRequest;
import Quickbuy.application.entity.Product;
import Quickbuy.application.repository.ProductRepository;
import Quickbuy.application.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    @Override
    public String saveProduct(SaveUpdateProductRequest saveUpdateProductRequest) {

        productRepository.findByName(saveUpdateProductRequest.getName()).ifPresent(existingProduct -> {
            throw new RuntimeException("Product with the same name already exists, Kindly choose a different name");
        });

        // Implement logic to save the product to the database
        Product product = new Product();
        product.setName(saveUpdateProductRequest.getName().trim());
        product.setDescription(saveUpdateProductRequest.getDescription().trim());
        product.setPrice(BigDecimal.valueOf(saveUpdateProductRequest.getPrice()));
        product.setImageUrl(saveUpdateProductRequest.getImageUrl().trim());
        productRepository.save(product);
        return "Product saved successfully";

    }

    @Override
    public String updateProduct(SaveUpdateProductRequest saveUpdateProductRequest, Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product with the given ID does not exist")
                );
        if (!product.getName().equals(saveUpdateProductRequest.getName())) {
            productRepository.findByName(saveUpdateProductRequest.getName()).ifPresent(existingProduct -> {
                throw new RuntimeException("Product with the same name already exists, Kindly choose a different name");
            });
        }
        product.setName(saveUpdateProductRequest.getName().trim());
        product.setDescription(saveUpdateProductRequest.getDescription().trim());
        product.setPrice(BigDecimal.valueOf(saveUpdateProductRequest.getPrice()));
        product.setImageUrl(saveUpdateProductRequest.getImageUrl().trim());
        productRepository.save(product);
        return "Product updated successfully";
    }


}
