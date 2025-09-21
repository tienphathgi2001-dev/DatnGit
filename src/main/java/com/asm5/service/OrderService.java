package com.asm5.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.asm5.model.Account;
import com.asm5.model.CartDetail;
import com.asm5.model.Order;
import com.asm5.model.OrderDetail;
import com.asm5.repository.OrderRepository;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public Order getOrderById(int id) {
        return orderRepository.findById(id).orElse(null);
    }
    
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
    
    public void deleteOrder(int id) {
        orderRepository.deleteById(id);
    }
  public Order createOrder(List<CartDetail> cartDetails, String paymentMethod, Account account) {
    Order order = new Order();
    order.setAccount(account);

    // Map paymentMethod String -> int
    // Ví dụ: "CASH" = 1, "CARD" = 2, ...
    int paymentCode;
    switch (paymentMethod.toUpperCase()) {
        case "CASH" -> paymentCode = 1;
        case "CARD" -> paymentCode = 2;
        default -> paymentCode = 0; // Unknown
    }
    order.setPaymentMethod(paymentCode);

    order.setCreatedDate(new Date());
    order.setStatus(0); // mới tạo
    order.setPaymentStatus(false);

    // Tính tổng tiền từ totalAmount trong CartDetail
    int total = cartDetails.stream()
            .map(cd -> cd.getTotalAmount().intValue())
            .reduce(0, Integer::sum);
    order.setTotal(total);

    // Convert CartDetail -> OrderDetail
    List<OrderDetail> orderDetails = cartDetails.stream().map(cd -> {
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setProduct(cd.getProduct());
        detail.setQuantity(cd.getQuantity());
        detail.setPrice(cd.getTotalAmount().intValue()); // dùng totalAmount
        return detail;
    }).toList();

    order.setOrderDetails(orderDetails);

    return orderRepository.save(order);
}

}
