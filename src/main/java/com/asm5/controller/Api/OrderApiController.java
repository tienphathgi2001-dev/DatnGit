package com.asm5.controller.Api;

import com.asm5.model.Order;
import com.asm5.model.OrderDetail;
import com.asm5.repository.OrderDetailRepository;
import com.asm5.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")  // ✅ endpoint cho Swagger test
public class OrderApiController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    // Lấy danh sách order
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Lấy 1 order theo id
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id " + id));
    }

    // Lấy chi tiết order
    @GetMapping("/{id}/details")
    public List<OrderDetail> getOrderDetails(@PathVariable Integer id) {
        return orderDetailRepository.findByOrderId(id);
    }

    // Tạo order
    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderRepository.save(order);
    }

    // Cập nhật order
    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable Integer id, @RequestBody Order order) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        existing.setCode(order.getCode());
        existing.setCreatedDate(order.getCreatedDate());
        existing.setTotal(order.getTotal());
        existing.setPaymentMethod(order.getPaymentMethod());
        existing.setPaymentStatus(order.getPaymentStatus());
        existing.setStatus(order.getStatus());
        existing.setDiscount(order.getDiscount());
        existing.setDiscountCode(order.getDiscountCode());
        existing.setFeeship(order.getFeeship());

        return orderRepository.save(existing);
    }

    // Xóa order
    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        orderRepository.delete(order);
    }
}
