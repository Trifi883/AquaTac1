package com.example.aquatac1.contoller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/business-owner")
@PreAuthorize("hasRole('BUSINESS_OWNER')")
public class BusinessOwnerController {
    @GetMapping("/dashboard")
    public String businessOwnerDashboard() {
        return "Business Owner Dashboard";
    }

    @GetMapping("/status")
    public String checkApprovalStatus() {
        return "Your account is approved and active";
    }
}