package com.asm5.service;

import com.asm5.model.DiscountCode;
import com.asm5.repository.DiscountCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiscountCodeService {

    private final DiscountCodeRepository discountCodeRepository;

    public Map<String, Object> applyDiscount(String code, double orderTotal) {
        Map<String, Object> result = new HashMap<>();

        DiscountCode discount = discountCodeRepository.findByCode(code).orElse(null);

        if (discount == null || discount.getActive() == null || !discount.getActive()) {
            result.put("success", false);
            result.put("message", "Mã khuyến mãi không hợp lệ!");
            return result;
        }

        if (discount.getExpirationDate() != null &&
            discount.getExpirationDate() < Instant.now().toEpochMilli()) {
            result.put("success", false);
            result.put("message", "Mã khuyến mãi đã hết hạn!");
            return result;
        }

        if (discount.getMaxUses() != null && discount.getUsedCount() != null &&
            discount.getUsedCount() >= discount.getMaxUses()) {
            result.put("success", false);
            result.put("message", "Mã khuyến mãi đã đạt số lần sử dụng tối đa!");
            return result;
        }

        // Tính toán giảm giá
        double discountValue = 0;
        if (discount.getDiscountPercent() != null && discount.getDiscountPercent() > 0) {
            discountValue = orderTotal * discount.getDiscountPercent() / 100.0;
        } else if (discount.getDiscountAmount() != null && discount.getDiscountAmount() > 0) {
            discountValue = discount.getDiscountAmount();
        }

        double finalTotal = orderTotal - discountValue;
        if (finalTotal < 0) finalTotal = 0;

        // Update used_count
        discount.setUsedCount(discount.getUsedCount() == null ? 1 : discount.getUsedCount() + 1);
        discountCodeRepository.save(discount);

        result.put("success", true);
        result.put("message", "Áp dụng mã thành công!");
        result.put("discount", discountValue);
        result.put("finalTotal", finalTotal);

        return result;
    }
}
