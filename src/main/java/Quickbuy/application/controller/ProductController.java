package Quickbuy.application.controller;

import Quickbuy.application.dto.ProductListResponse;
import Quickbuy.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductListResponse> getAllProducts() {
        // This method will return a list of products.
        // For now, we can return an empty list or mock data.\

        log.info("Fetching all products");
        return productService.getAllProducts();

    }
}
