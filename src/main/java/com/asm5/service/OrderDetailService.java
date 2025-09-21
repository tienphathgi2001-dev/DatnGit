package com.asm5.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.asm5.model.OrderDetail;
import com.asm5.repository.OrderDetailRepository;

@Service
public class OrderDetailService {
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    public List<OrderDetail> getAllOrderDetails() {
        return orderDetailRepository.findAll();
    }
    
    public OrderDetail getOrderDetailById(int id) {
        return orderDetailRepository.findById(id).orElse(null);
    }
    
    public OrderDetail saveOrderDetail(OrderDetail orderDetail) {
        return orderDetailRepository.save(orderDetail);
    }
    
    public void deleteOrderDetail(int id) {
        orderDetailRepository.deleteById(id);
    }
    
    public List<OrderDetail> getOrderDetailsByOrderId(int orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
    
    public List<OrderDetail> getOrderDetailsByProductId(int productId) {
        return orderDetailRepository.findByProductId(productId);
    }
}
