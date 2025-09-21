package com.asm5.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.asm5.config.Config;
import com.asm5.model.Account;
import com.asm5.model.CartDetail;
import com.asm5.model.Order;
import com.asm5.model.OrderDetail;
import com.asm5.model.Product;
import com.asm5.repository.CartDetailRepository;
import com.asm5.repository.OrderDetailRepository;
import com.asm5.repository.OrderRepository;
import com.asm5.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/vnpay")
public class VNPayController {

    private final ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    VNPayController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ================= Tạo URL Thanh toán =================
    private String buildPaymentUrl(long amountParam, String bankCode, String language, HttpServletRequest request,
                                   String txnRef) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";

        long amount = amountParam * 100; // nhân 100 theo yêu cầu VNPAY
        String vnp_IpAddr = Config.getIpAddress(request);
        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + txnRef);
        vnp_Params.put("vnp_OrderType", orderType);

        if (language != null && !language.isEmpty()) {
            vnp_Params.put("vnp_Locale", language);
        } else {
            vnp_Params.put("vnp_Locale", "vn");
        }

        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sắp xếp và tạo chuỗi hash
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return Config.vnp_PayUrl + "?" + queryUrl;
    }

    // ================= API tạo đơn hàng + redirect sang VNPAY =================
    @GetMapping("/create")
public void createAndRedirect(
        @RequestParam("amount") long amount,
        @RequestParam(value = "bankCode", required = false) String bankCode,
        @RequestParam(value = "language", required = false) String language,
        HttpServletRequest request,
        @RequestParam("shippingAddress") String shippingAddress,
        HttpServletResponse response,
        HttpSession session) throws IOException {

    String txnRef = Config.getRandomNumber(8);

    // Lấy khách hàng từ session
    Account accountLogin = (Account) session.getAttribute("user");
    if (accountLogin == null) {
        response.sendRedirect("/login");
        return;
    }

    // Lấy giỏ hàng từ DB
    List<CartDetail> cartDetails = cartDetailRepository.findByAccount(accountLogin);
    if (cartDetails == null || cartDetails.isEmpty()) {
        response.sendRedirect("/cart");
        return;
    }

    // Tạo order
    Order order = new Order();
    order.setCode(txnRef);
    order.setTotal((int) amount);
    order.setPaymentStatus(false);
    order.setStatus(1); // Chờ thanh toán
    order.setCreatedDate(new Date());
    order.setAccount(accountLogin);
    order.setPaymentMethod(1); // 1 = VNPAY
    order.setAddress(shippingAddress); 
    orderRepository.save(order);

    // Lưu chi tiết hóa đơn
    for (CartDetail item : cartDetails) {
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setProduct(item.getProduct());
        detail.setQuantity(item.getQuantity());
        detail.setPrice(item.getProduct().getPrice());
        orderDetailRepository.save(detail);
    }

    String paymentUrl = buildPaymentUrl(amount, bankCode, language, request, txnRef);
    response.sendRedirect(paymentUrl);
}


    // ================= API xử lý khi VNPAY trả kết quả =================
    @GetMapping("/return")
public String paymentReturn(@RequestParam Map<String, String> params,
                            Model model,
                            HttpSession session) {

    boolean valid = Config.validateSignature(params);
    String txnRef = params.get("vnp_TxnRef");
    String rspCode = params.get("vnp_ResponseCode");

    Optional<Order> opt = orderRepository.findByCode(txnRef);
    if (opt.isEmpty()) {
        model.addAttribute("message", "Đơn hàng không tồn tại!");
        return "error";
    }

    Order order = opt.get();

    // Lấy chi tiết hóa đơn
    List<OrderDetail> orderDetails = orderDetailRepository.findByOrderIdWithProduct(order.getId());

    if ("00".equals(rspCode)) { // Thanh toán thành công
    order.setPaymentStatus(true);
    order.setStatus(1); // ✅ 1 = Chờ xác nhận
    orderRepository.save(order);


        // Trừ số lượng tồn kho
        for (OrderDetail detail : orderDetails) {
            Product product = detail.getProduct();
            int currentQty = product.getQuantity();
            int purchasedQty = detail.getQuantity();
            product.setQuantity(currentQty - purchasedQty);
            productRepository.save(product);
        }

        // Xóa giỏ hàng
        Account accountLogin = order.getAccount();
        cartDetailRepository.deleteByAccount(accountLogin);

        session.removeAttribute("cart");
        model.addAttribute("message", "Thanh toán thành công! Đang chuyển hướng...");
    } else { 
        order.setStatus(3);
        orderRepository.save(order);
        model.addAttribute("message", "Thanh toán thất bại! Vui lòng thử lại.");
    }

    model.addAttribute("order", order);
    model.addAttribute("orderDetails", orderDetails);

    return "invoice";
}


}
