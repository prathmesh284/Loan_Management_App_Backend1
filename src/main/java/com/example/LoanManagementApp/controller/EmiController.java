package com.example.LoanManagementApp.controller;

import com.example.LoanManagementApp.model.Emi;
import com.example.LoanManagementApp.service.EmiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emis")
public class EmiController {

    @Autowired
    private EmiService emiService;

    // ✅ PAY EMI
    @PostMapping("/pay/{loanId}")
    public Emi payEmi(
            @PathVariable Long loanId,
            @RequestParam(defaultValue = "CASH") String method
    ) {
        return emiService.payEmi(loanId, method);
    }

    @PostMapping("/pay")
    public ResponseEntity<?> payEmi(@RequestBody Emi request) {
        return ResponseEntity.ok(emiService.payEmi(request));
    }

    // ✅ GET EMI HISTORY
    @GetMapping("/{loanId}")
    public List<Emi> getEmiHistory(@PathVariable Long loanId) {
        return emiService.getEmiHistory(loanId);
    }
}