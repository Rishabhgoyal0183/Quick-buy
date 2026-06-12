package Quickbuy.application.controller;

import Quickbuy.application.dto.ProductListResponse;
import Quickbuy.application.dto.SaveUpdateProductRequest;
import Quickbuy.application.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/save")
    public ResponseEntity<String> saveProduct(@RequestBody @Valid SaveUpdateProductRequest saveUpdateProductRequest,
                                      BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            // Handle validation errors
            log.error("Validation errors: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body("Invalid product data");
        }

        // This method will save a product. For now, we can return a success message.
        log.info("Saving a new product");
        return ResponseEntity.ok(productService.saveProduct(saveUpdateProductRequest));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateProduct(@RequestBody @Valid SaveUpdateProductRequest saveUpdateProductRequest,
                                              BindingResult bindingResult, @PathVariable(required = true, name = "id") Long id) {

        if (bindingResult.hasErrors()) {
            // Handle validation errors
            log.error("Validation errors: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body("Invalid product data");
        }

        // This method will save a product. For now, we can return a success message.
        log.info("Updating product with ID: {}", id);
        return ResponseEntity.ok(productService.updateProduct(saveUpdateProductRequest, id));
    }
}
