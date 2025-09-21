package com.asm5.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.asm5.model.Account;
import com.asm5.model.Address;
import com.asm5.model.CartDetail;
import com.asm5.model.DiscountCode;
import com.asm5.repository.AccountRepository;
import com.asm5.repository.DiscountCodeRepository;
import com.asm5.service.CartDetailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CheckOutController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CartDetailService cartDetailService;

    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpSession session) {
        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null)
            return "redirect:/login";

        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null)
            return "redirect:/login";

        // Lấy danh sách giỏ hàng
        List<CartDetail> cartDetails = cartDetailService.getCartDetailsByAccountId(accountId);
        if (cartDetails == null)
            cartDetails = new ArrayList<>();

        session.setAttribute("cartDetails", cartDetails);

        // Danh sách địa chỉ
        List<Address> addresses = account.getAddresses();
        if (addresses == null)
            addresses = new ArrayList<>();

        // Tổng tiền hàng
        int total = cartDetails.stream()
                .mapToInt(cd -> cd.getProduct().getPrice() * cd.getQuantity())
                .sum();

        // Tổng số lượng sản phẩm
        int totalQuantity = cartDetails.stream()
                .mapToInt(CartDetail::getQuantity)
                .sum();

        // Danh sách selectedIds
        List<Integer> selectedIds = cartDetails.stream()
                .map(cd -> cd.getProduct().getId())
                .collect(Collectors.toList());

        // Địa chỉ mặc định
        String deliveryAddress = "Chưa cập nhật";
        if (!addresses.isEmpty()) {
            Address address = addresses.get(0);
            StringBuilder sb = new StringBuilder();
            if (address.getHouseNumber() != null)
                sb.append(address.getHouseNumber()).append(", ");
            if (address.getWard() != null) {
                sb.append(address.getWard().getTen()).append(", ");
                if (address.getWard().getDistrict() != null) {
                    sb.append(address.getWard().getDistrict().getName()).append(", ");
                    if (address.getWard().getDistrict().getProvince() != null) {
                        sb.append(address.getWard().getDistrict().getProvince().getName());
                    }
                }
            }
            deliveryAddress = sb.toString();
        }

        // Tính phí vận chuyển và tổng cộng
        int shippingFee = 0;
        int grandTotal = total + shippingFee;

        session.setAttribute("totalAmount", grandTotal);

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("addresses", addresses);
        model.addAttribute("selectedIds", selectedIds);
        model.addAttribute("total", total);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("deliveryAddress", deliveryAddress);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("grandTotal", grandTotal);

        return "checkout";
    }

    @PostMapping("/api/discount/apply")
    @ResponseBody
    public Map<String, Object> applyDiscount(
            @RequestParam("code") String code,
            @RequestParam("orderTotal") int orderTotal) {

        DiscountCode discount = discountCodeRepository.findByCodeAndActiveTrue(code);

        if (discount == null) {
            return Map.of(
                    "success", false,
                    "message", "Mã khuyến mãi không hợp lệ!");
        }

        // Check ngày hết hạn
        long now = System.currentTimeMillis();
        if (discount.getExpirationDate() != null && discount.getExpirationDate() < now) {
            return Map.of(
                    "success", false,
                    "message", "Mã khuyến mãi đã hết hạn!");
        }

        // Check số lượt sử dụng
        if (discount.getMaxUses() != null && discount.getUsedCount() >= discount.getMaxUses()) {
            return Map.of(
                    "success", false,
                    "message", "Mã khuyến mãi đã hết lượt sử dụng!");
        }

        // Tính giá trị giảm giá
        int discountValue = 0;

        // Ưu tiên discountAmount nếu > 0
        if (discount.getDiscountAmount() != null && discount.getDiscountAmount() > 0) {
            discountValue = discount.getDiscountAmount().intValue();
        }
        // Nếu không có tiền giảm thì dùng % giảm
        else if (discount.getDiscountPercent() != null && discount.getDiscountPercent() > 0) {
            discountValue = orderTotal * discount.getDiscountPercent() / 100;
        }

        // Không cho giảm quá tổng đơn
        if (discountValue > orderTotal) {
            discountValue = orderTotal;
        }

        int finalTotal = orderTotal - discountValue;

        // Cập nhật lượt sử dụng
        discount.setUsedCount(discount.getUsedCount() + 1);
        discountCodeRepository.save(discount);

        return Map.of(
                "success", true,
                "message", "Áp dụng mã thành công!",
                "discount", discountValue,
                "finalTotal", finalTotal);
    }

    @GetMapping("/checkout/pay")
    public String payWithVNPay(HttpSession session) {
        Integer amount = (Integer) session.getAttribute("totalAmount");
        if (amount == null || amount <= 0) {
            return "redirect:/checkout";
        }
        return "redirect:/vnpay/create?amount=" + amount;
    }
}
