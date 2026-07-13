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
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
                !("root".equalsIgnoreCase(authentication.getName()) || 
                  "root@techheaven.com".equalsIgnoreCase(authentication.getName()) || 
                  "root@teachheaven.com".equalsIgnoreCase(authentication.getName()))) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Only root administrator can access this panel", null));
        }
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard data retrieved", adminService.getDashboard()));
    }
}
