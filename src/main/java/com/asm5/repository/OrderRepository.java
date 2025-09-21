package com.asm5.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.asm5.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByCode(String code);

    @Query("SELECT a.id FROM Account a WHERE a.userName = :username")
    Integer findAccountIdByUsername(@Param("username") String username);

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.account", countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> findAllWithAccount(Pageable pageable);

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.account WHERE o.paymentStatus = true", countQuery = "SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = true")
    Page<Order> findPaidOrders(Pageable pageable);

    @Query(value = "SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.account " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product", countQuery = "SELECT COUNT(o) FROM Order o")
    Page<Order> findAllWithAccountAndDetails(Pageable pageable);

    Page<Order> findByAccount_Id(Integer accountId, Pageable pageable);

    // List<Order> findByAccount_Id(Integer accountId);
    Page<Order> findByAccount_IdAndCreatedDateBetween(Integer accountId, Timestamp from, Timestamp to,
            Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.account.id = :accountId AND o.createdDate BETWEEN :fromDate AND :toDate AND (o.status = 0 OR o.status = 2)")
    Page<Order> findHistoryOrdersBetween(@Param("accountId") Integer accountId,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            Pageable pageable);

    // Đơn hàng hiện tại
    @Query("SELECT o FROM Order o WHERE o.account.id = :userId AND o.status IN (1,2,3) ORDER BY o.createdDate DESC")
    Page<Order> findCurrentOrders(@Param("userId") Integer userId, Pageable pageable);

    // Lịch sử đơn hàng
    @Query("SELECT o FROM Order o WHERE o.account.id = :userId AND o.status IN (0,4) ORDER BY o.createdDate DESC")
    Page<Order> findHistoryOrders(@Param("userId") Integer userId, Pageable pageable);

    List<Order> findByAccount_Id(Integer accountId);

    List<Order> findByAccountIdAndStatusIn(Integer accountId, List<Integer> statuses);

     // Đơn hàng đang xử lý (trạng thái 1,2,3)
    List<Order> findByAccount_IdAndStatusIn(Integer accountId, List<Integer> statuses);

    // Lịch sử mua hàng (trạng thái 0 = hủy, 4 = giao thành công)
    List<Order> findByAccount_IdAndStatusInOrderByCreatedDateDesc(Integer accountId, List<Integer> statuses);

    // Nếu cần phân trang cho lịch sử
    Page<Order> findByAccount_IdAndStatusIn(Integer accountId, List<Integer> statuses, Pageable pageable);

       Page<Order> findByAccount_IdAndStatusInAndCreatedDateBetween(
            Integer accountId, List<Integer> statuses, Timestamp from, Timestamp to, Pageable pageable);

}
