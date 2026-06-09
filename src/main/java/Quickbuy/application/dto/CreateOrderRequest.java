package Quickbuy.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String customerEmail;
}
