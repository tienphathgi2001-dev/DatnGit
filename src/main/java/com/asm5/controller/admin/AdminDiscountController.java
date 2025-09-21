package com.asm5.controller.admin;

import com.asm5.model.DiscountCode;
import com.asm5.repository.DiscountCodeRepository;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/discounts")
public class AdminDiscountController {

    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    // Trang danh sách mã giảm giá
    @GetMapping
    public String listDiscounts(Model model) {
        model.addAttribute("discounts", discountCodeRepository.findAll());
        return "admin/discounts/list"; // view: src/main/resources/templates/admin/discounts/list.html
    }

    @GetMapping("/add")
    public String showCreateForm(Model model) {
        DiscountCode discountCode = new DiscountCode();

        // Giá trị mặc định
        discountCode.setActive(true); // checkbox Hoạt động mặc định checked
        discountCode.setDiscountAmount(0.0);
        discountCode.setDiscountPercent(0); // % giảm mặc định
        discountCode.setMaxUses(1); // số lần sử dụng tối đa mặc định
        discountCode.setUsedCount(0); // đã sử dụng 0 lần

        model.addAttribute("discountCode", discountCode);
        return "admin/discounts/add";
    }

    @PostMapping("/save")
    public String saveDiscount(
            @RequestParam("code") String code,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "discountAmount", required = false) Double discountAmount,
            @RequestParam(value = "discountPercent", required = false) Integer discountPercent,
            @RequestParam(value = "maxUses", required = false) Integer maxUses,
            @RequestParam("expirationDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate) {

        DiscountCode discountCode = new DiscountCode();
        discountCode.setCode(code);
        discountCode.setDescription(description);
        discountCode.setUsedCount(0);

        // checkbox
        discountCode.setActive(active != null ? active : false);

        // số tiền / % giảm / max uses
        discountCode.setDiscountAmount(discountAmount != null ? discountAmount : 0.0);
        discountCode.setDiscountPercent(discountPercent != null ? discountPercent : 0);
        discountCode.setMaxUses(maxUses != null ? maxUses : 1);

        // expirationDate -> Long
        if (expirationDate != null) {
            discountCode.setExpirationDate(
                    expirationDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        discountCodeRepository.save(discountCode);
        return "redirect:/admin/discounts";
    }

    // Xóa mã giảm giá
    @GetMapping("/delete/{id}")
    public String deleteDiscount(@PathVariable Integer id) {
        discountCodeRepository.deleteById(id);
        return "redirect:/admin/discounts";
    }
}
