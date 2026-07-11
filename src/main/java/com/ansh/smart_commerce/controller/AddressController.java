package com.ansh.smart_commerce.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ansh.smart_commerce.dto.AddressRequest;
import com.ansh.smart_commerce.dto.AddressResponse;
import com.ansh.smart_commerce.dto.ApiResponse;
import com.ansh.smart_commerce.service.AddressService;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @RequestParam(required = false) Long userId,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.addAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added successfully", response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(@PathVariable Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success("Addresses retrieved", addressService.getAddressesByUser(userId)));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Address updated", addressService.updateAddress(addressId, request)));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(@PathVariable Long addressId) {
        return ResponseEntity.ok(
                ApiResponse.success("Default address updated", addressService.setDefault(addressId)));
    }
}
