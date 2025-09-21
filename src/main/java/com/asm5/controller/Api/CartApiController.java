package com.asm5.controller.Api;

import com.asm5.model.*;
import com.asm5.repository.*;
import com.asm5.service.CartDetailService;
import com.asm5.service.AddressService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

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
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestParam Integer accountId,
                                       @RequestParam Integer productId,
                                       @RequestParam(defaultValue = "1") Integer quantity) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (accountOpt.isEmpty() || productOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Account hoặc Product không tồn tại");
        }

        cartDetailService.addToCart(accountId, productId, quantity);
        return ResponseEntity.ok("Đã thêm sản phẩm vào giỏ hàng");
    }

    // -------------------- Xem giỏ hàng --------------------
    @GetMapping("/view/{accountId}")
    public ResponseEntity<?> viewCart(@PathVariable Integer accountId) {
        List<CartDetail> cartDetails = cartDetailService.getCartDetailsByAccount(accountId);
        int total = cartDetails.stream()
                .mapToInt(cd -> cd.getProduct().getPrice() * cd.getQuantity())
                .sum();

        Map<String, Object> response = new HashMap<>();
        response.put("cartDetails", cartDetails);
        response.put("total", total);

        return ResponseEntity.ok(response);
    }

    // -------------------- Cập nhật số lượng --------------------
    @PutMapping("/update")
    public ResponseEntity<?> updateCart(@RequestParam Integer accountId,
                                        @RequestParam Integer productId,
                                        @RequestParam String action) {

        CartDetail cd = cartDetailService.findById(accountId, productId);
        if (cd == null) {
            return ResponseEntity.badRequest().body("Sản phẩm không tồn tại trong giỏ hàng!");
        }

        Product product = cd.getProduct();
        if ("increase".equals(action)) {
            if (cd.getQuantity() + 1 > product.getQuantity()) {
                return ResponseEntity.badRequest().body("Số lượng trong kho không đủ!");
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
                return ResponseEntity.ok("Sản phẩm đã bị xóa khỏi giỏ hàng!");
            }
        }

        return ResponseEntity.ok(cd);
    }

    // -------------------- Xóa sản phẩm --------------------
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestParam Integer accountId,
                                            @RequestParam Integer productId) {
        cartDetailService.removeCartDetail(accountId, productId);
        return ResponseEntity.ok("Đã xóa sản phẩm khỏi giỏ hàng!");
    }

    // -------------------- Checkout --------------------
    @PostMapping("/checkout")
    @Transactional
    public ResponseEntity<?> processCheckout(@RequestParam Integer accountId,
                                             @RequestParam int paymentMethod,
                                             @RequestParam(required = false) String bankCode) throws Exception {

        List<CartDetail> cartDetails = cartDetailService.getCartDetailsByAccount(accountId);
        if (cartDetails.isEmpty()) {
            return ResponseEntity.badRequest().body("Giỏ hàng trống!");
        }

        // Tổng tiền
        long totalAmount = cartDetails.stream()
                .mapToLong(cd -> (long) cd.getProduct().getPrice() * cd.getQuantity())
                .sum();

        // Tạo Order
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

        // Tạo OrderDetail
        for (CartDetail cd : cartDetails) {
            if (cd.getProduct() == null) continue;
            OrderDetail od = new OrderDetail();
            od.setOrder(order);
            od.setProduct(cd.getProduct());
            od.setQuantity(cd.getQuantity());
            od.setPrice(cd.getProduct().getPrice());
            orderDetailRepository.save(od);
        }

        // Xóa giỏ hàng
        cartDetailService.deleteAllByAccountId(accountId);

        // Nếu là thanh toán online -> trả URL VNPay
        if (paymentMethod == 2) {
            long vnpAmount = totalAmount * 100;
            String url = "/vnpay/create?amount=" + vnpAmount +
                    "&txnRef=" + URLEncoder.encode(txnRef, StandardCharsets.UTF_8);
            if (bankCode != null && !bankCode.isEmpty()) {
                url += "&bankCode=" + URLEncoder.encode(bankCode, StandardCharsets.UTF_8);
            }
            return ResponseEntity.ok(Map.of("redirectUrl", url));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Thanh toán thành công",
                "orderId", order.getId(),
                "total", totalAmount
        ));
    }
}
