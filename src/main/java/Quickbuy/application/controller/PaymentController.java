package Quickbuy.application.controller;

import Quickbuy.application.dto.PaymentFailureRequest;
import Quickbuy.application.dto.PaymentVerifyRequest;
import Quickbuy.application.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@Valid @RequestBody PaymentVerifyRequest paymentVerifyRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Invalid request");
            return ResponseEntity.badRequest().body(errorMessage);
        }

        return ResponseEntity.ok(paymentService.verifyPayment(paymentVerifyRequest));
    }

    @PostMapping("/failure")
    public ResponseEntity<String> paymentFailure(@Valid @RequestBody PaymentFailureRequest request,
                                               BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Invalid request");
            return ResponseEntity.badRequest().body(errorMessage);

        }
        try {
            String response = paymentService.markPaymentFailed(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while marking payment as failed: " + e.getMessage());
        }
    }
}
