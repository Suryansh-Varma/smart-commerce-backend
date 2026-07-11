package com.ansh.smart_commerce.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ansh.smart_commerce.dto.ApiResponse;
import com.ansh.smart_commerce.dto.LoginRequest;
import com.ansh.smart_commerce.dto.LoginResponse;
import com.ansh.smart_commerce.dto.UserResponse;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.security.JwtService;
import com.ansh.smart_commerce.service.UserService;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody User user) {
        User saved = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", UserResponse.from(saved)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = UserResponse.from(userService.getUserById(id));
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        
    User user = userService.login(loginRequest.getEmail(),loginRequest.getPassword());
        System.out.println("User ID: " + user.getId());
System.out.println("User Name: " + user.getName());
System.out.println("User Email: " + user.getEmail());
    String token = jwtService.generateToken(user.getEmail());

    LoginResponse loginResponse = new LoginResponse(
            token,
            user.getId(),
            user.getName(),
            user.getEmail()
    );

    return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse)
    );
}
}