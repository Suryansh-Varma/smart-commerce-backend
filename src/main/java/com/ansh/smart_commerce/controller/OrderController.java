package com.ansh.smart_commerce.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ansh.smart_commerce.dto.ApiResponse;
import com.ansh.smart_commerce.dto.OrderResponse;
import com.ansh.smart_commerce.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping({"/place", "/place/{userId}"})
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@PathVariable(required = false) Long userId) {
        OrderResponse response = orderService.placeOrder(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", response));
    }

    @GetMapping({"/user", "/user/{userId}"})
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrderHistory(@PathVariable(required = false) Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success("Order history retrieved successfully", orderService.getOrderHistory(userId)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                ApiResponse.success("Order retrieved successfully", orderService.getOrderById(orderId)));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                ApiResponse.success("Order cancelled successfully", orderService.cancelOrder(orderId)));
    }

    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long orderId) {
        byte[] pdfBytes = orderService.generateInvoicePdf(orderId);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment()
                .filename("invoice-" + orderId + ".pdf")
                .build());
        return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
}
