package com.asm5.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asm5.model.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer>{
    List<OrderDetail> findByOrderId(int orderId);
    List<OrderDetail> findByProductId(int productId);

     @Query("SELECT od FROM OrderDetail od JOIN FETCH od.product WHERE od.order.id = :orderId")
    List<OrderDetail> findByOrderIdWithProduct(@Param("orderId") Integer orderId);



}   
