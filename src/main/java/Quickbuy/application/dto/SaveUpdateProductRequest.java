package Quickbuy.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SaveUpdateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Product description is required")
    @Size(max = 255, message = "Product description must be less than 255 characters")
    private String description;

    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Product price must be a positive number")
    private double price;

    @NotBlank(message = "Product image URL is required")
    @Size(max = 255, message = "Product image URL must be less than 255 characters")
    private String imageUrl;
}
