package com.asm5.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.asm5.model.*;
import com.asm5.repository.*;
import com.asm5.service.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartDetailService cartDetailService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private AddressService addressService;

    // -------------------- Thêm sản phẩm vào giỏ --------------------
    @GetMapping("/add")
    public String addToCart(@RequestParam("productId") Integer productId,
                            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                            HttpSession session) {

        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) return "redirect:/taikhoan";

        Optional<Account> accountOpt = accountRepository.findById(accountId);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (accountOpt.isEmpty() || productOpt.isEmpty()) return "redirect:/cart/view";

        cartDetailService.addToCart(accountId, productId, quantity);

        return "redirect:/cart/view";
    }

    // -------------------- Xem giỏ hàng --------------------
    @GetMapping("/view")
    public String viewCart(Model model, HttpSession session) {
        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) return "redirect:/taikhoan";

        List<CartDetail> cartDetails = cartDetailService.getCartDetailsByAccount(accountId);
        int total = 0;
        if (cartDetails != null && !cartDetails.isEmpty()) {
            total = cartDetails.stream()
                    .mapToInt(cd -> cd.getProduct().getPrice() * cd.getQuantity())
                    .sum();
        }
        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("total", total);
        return "cart";
    }

    // -------------------- Cập nhật số lượng --------------------
    @PostMapping("/update")
    public String updateCart(@RequestParam("accountId") Integer accountId,
                             @RequestParam("productId") Integer productId,
                             @RequestParam("action") String action,
                             RedirectAttributes redirectAttributes) {

        CartDetail cd = cartDetailService.findById(accountId, productId);
        if (cd == null) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại trong giỏ hàng!");
            return "redirect:/cart/view";
        }

        Product product = cd.getProduct();
        if ("increase".equals(action)) {
            if (cd.getQuantity() + 1 > product.getQuantity()) {
                redirectAttributes.addFlashAttribute("error", "Số lượng trong kho không đủ!");
            } else {
                cd.setQuantity(cd.getQuantity() + 1);
                cartDetailService.save(cd);
            }
        } else if ("decrease".equals(action)) {
            if (cd.getQuantity() > 1) {
                cd.setQuantity(cd.getQuantity() - 1);
                cartDetailService.save(cd);
            } else {
                cartDetailService.delete(cd);
                redirectAttributes.addFlashAttribute("message", "Sản phẩm đã được xóa khỏi giỏ hàng!");
            }
        }

        return "redirect:/cart/view";
    }

    // -------------------- Xóa sản phẩm --------------------
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("accountId") Integer accountId,
                                 @RequestParam("productId") Integer productId,
                                 RedirectAttributes redirectAttributes) {
        cartDetailService.removeCartDetail(accountId, productId);
        redirectAttributes.addFlashAttribute("message", "Sản phẩm đã được xóa khỏi giỏ hàng!");
        return "redirect:/cart/view";
    }

    // -------------------- Checkout - hiển thị --------------------
    @GetMapping("/checkout")
    public String showCheckout(Model model, HttpSession session) {
        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) return "redirect:/taikhoan";

        List<CartDetail> cartDetails = cartDetailService.getCartDetailsByAccount(accountId);
        if (cartDetails.isEmpty()) {
            model.addAttribute("error", "Giỏ hàng trống!");
            return "cart";
        }

        int total = cartDetails.stream()
                .mapToInt(cd -> cd.getProduct().getPrice() * cd.getQuantity())
                .sum();

        List<Address> addresses = addressService.getAddressesByAccountId(accountId);

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("total", total);
        model.addAttribute("addresses", addresses);

        return "checkout";
    }

    // -------------------- Checkout - xử lý thanh toán --------------------
    @PostMapping("/checkout")
    @Transactional
    public String processCheckout(@RequestParam("paymentMethod") int paymentMethod,
                                  @RequestParam(value = "bankCode", required = false) String bankCode,
                                  HttpSession session,
                                  RedirectAttributes redirectAttrs) throws Exception {

        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) return "redirect:/taikhoan";

        List<CartDetail> cartDetails = cartDetailService.getCartDetailsByAccount(accountId);
        if (cartDetails.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "Giỏ hàng trống!");
            return "redirect:/cart/view";
        }

        // Tổng tiền
        long totalAmount = cartDetails.stream()
                .mapToLong(cd -> (long) cd.getProduct().getPrice() * cd.getQuantity())
                .sum();

        // 1️⃣ Tạo Order
        String txnRef = "ORDER-" + System.currentTimeMillis();
        Order order = new Order();
        order.setCode(txnRef);
        order.setAccount(accountRepository.findById(accountId).orElse(null));
        order.setCreatedDate(new Date());
        order.setTotal((int) totalAmount);
        order.setPaymentStatus(false);
        order.setStatus(1);
        order.setPaymentMethod(paymentMethod);
        orderRepository.save(order);

        // 2️⃣ Tạo OrderDetail từ giỏ hàng
        for (CartDetail cd : cartDetails) {
            if (cd.getProduct() == null) continue;
            OrderDetail od = new OrderDetail();
            od.setOrder(order);
            od.setProduct(cd.getProduct());
            od.setQuantity(cd.getQuantity());
            od.setPrice(cd.getProduct().getPrice());
            orderDetailRepository.save(od);
        }

        // 3️⃣ Xóa giỏ hàng
        cartDetailService.deleteAllByAccountId(accountId);

        // 4️⃣ Redirect sang VNPay nếu thanh toán online
        if (paymentMethod == 2) {
            long vnpAmount = totalAmount * 100; // VNPay yêu cầu *100
            String url = "redirect:/vnpay/create?amount=" + vnpAmount +
                    "&txnRef=" + URLEncoder.encode(txnRef, StandardCharsets.UTF_8);
            if (bankCode != null && !bankCode.isEmpty()) {
                url += "&bankCode=" + URLEncoder.encode(bankCode, StandardCharsets.UTF_8);
            }
            return url;
        }

        redirectAttrs.addFlashAttribute("error", "Phương thức thanh toán chưa được hỗ trợ");
        return "redirect:/cart/view";
    }
}
