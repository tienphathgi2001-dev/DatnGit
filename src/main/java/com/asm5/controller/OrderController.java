package com.asm5.controller;

import com.asm5.model.Account;
import com.asm5.model.Order;
import com.asm5.model.OrderDetail;
import com.asm5.model.Product;
import com.asm5.repository.AccountRepository;
import com.asm5.repository.OrderDetailRepository;
import com.asm5.repository.OrderRepository;
import com.asm5.repository.ProductRepository;
import com.asm5.service.MailService;
import com.asm5.service.MailService.Mail;

import jakarta.servlet.http.HttpSession;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
public class OrderController {

    private final ProductRepository productRepository;
    private final MailService mailService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    public OrderController(ProductRepository productRepository, MailService mailService) {
        this.productRepository = productRepository;
        this.mailService = mailService;
    }

    // Lịch sử đơn hàng
    @GetMapping("/orders")
    public String orderHistory(
            Model model,
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {

        Account user = (Account) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Order> ordersPage;

        List<Integer> historyStatuses = List.of(0, 4); // hủy hoặc giao thành công

        if (fromDate != null && toDate != null && !fromDate.isEmpty() && !toDate.isEmpty()) {
            Timestamp fromTs = Timestamp.valueOf(fromDate + " 00:00:00");
            Timestamp toTs = Timestamp.valueOf(toDate + " 23:59:59");
            ordersPage = orderRepository.findByAccount_IdAndStatusInAndCreatedDateBetween(
                    user.getId(), historyStatuses, fromTs, toTs, pageable);
        } else {
            ordersPage = orderRepository.findByAccount_IdAndStatusIn(user.getId(), historyStatuses, pageable);
        }

        model.addAttribute("orders", ordersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "order/order_history";
    }

    // Chi tiết đơn hàng
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable("id") Integer orderId, Model model, HttpSession session) {
        Account user = (Account) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getAccount().getId().equals(user.getId())) {
            return "redirect:/user/orders";
        }

        order.getOrderDetails().forEach(od -> od.getProduct().getImage());
        model.addAttribute("order", order);
        return "order/order_detail";
    }

    // Theo dõi đơn hàng
    @GetMapping("/track")
    public String trackOrders(Model model, HttpSession session) {
        Account user = (Account) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<Integer> allowedStatuses = List.of(1, 2, 3); // đang xử lý
        List<Order> orders = orderRepository.findByAccount_IdAndStatusIn(user.getId(), allowedStatuses);
        model.addAttribute("orders", orders != null ? orders : List.of());
        return "order/order_tracking";
    }

    // Hủy đơn hàng
    @PostMapping("/track/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Integer orderId, HttpSession session) {
        Account user = (Account) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getAccount().getId().equals(user.getId())) {
            return "redirect:/user/track";
        }

        if (order.getStatus() == 1) { // đang chờ xử lý
            List<OrderDetail> details = orderDetailRepository.findByOrderIdWithProduct(orderId);

            for (OrderDetail detail : details) {
                Product product = detail.getProduct();
                product.setQuantity(product.getQuantity() + detail.getQuantity());
                productRepository.save(product);
            }

            order.setStatus(0); // hủy
            orderRepository.save(order);

            // Gửi mail
            String subject = "Xác nhận hủy đơn hàng #" + order.getId();
            String body = "<h3>Xin chào " + user.getFullName() + ",</h3>"
                    + "<p>Đơn hàng <b>#" + order.getId() + "</b> của bạn đã được hủy thành công.</p>"
                    + "<p>Để hoàn tiền, vui lòng cung cấp thông tin tài khoản ngân hàng "
                    + "bằng cách trả lời email này:</p>"
                    + "<ul>"
                    + "<li>Ngân hàng</li>"
                    + "<li>Số tài khoản</li>"
                    + "<li>Chủ tài khoản</li>"
                    + "</ul>"
                    + "<p>Trân trọng,<br>WebShop</p>";

            mailService.send(user.getEmail(), subject, body);
        }

        return "redirect:/user/track";
    }
}



