package com.asm5.controller;

import com.asm5.model.CartDetail;
import com.asm5.model.District;
import com.asm5.model.Ward;
import com.asm5.repository.WardRepository;
import com.asm5.service.ShippingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;
    private final WardRepository wardRepository;

    @GetMapping("/fee")
    public ResponseEntity<Map<String, Object>> getShippingFee(
            @RequestParam String wardCode,
            @RequestParam String districtCode,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy giỏ hàng từ session
            List<CartDetail> cartDetails = (List<CartDetail>) session.getAttribute("cartDetails");
            if (cartDetails == null) cartDetails = List.of();

            // Truyền wardCode sang service, service sẽ tự lấy code của district
            int fee = shippingService.getShippingFee(wardCode, districtCode, cartDetails);

            response.put("fee", fee);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("fee", null);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
