package com.ansh.smart_commerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ansh.smart_commerce.dto.ApiResponse;
import com.ansh.smart_commerce.dto.DashboardResponse;
import com.ansh.smart_commerce.service.AdminService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard data retrieved", adminService.getDashboard()));
    }
}
