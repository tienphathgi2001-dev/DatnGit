package com.asm5.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.asm5.model.Product;

import java.util.Date;
import java.util.List;

public interface StatisticsRepository extends JpaRepository<Product, Long> {

        // 1️⃣ Tổng số sản phẩm
        @Query("SELECT COUNT(p) FROM Product p")
        Integer countTotalProducts();

        // 2️⃣ Tổng số đơn hàng
        @Query("SELECT COUNT(o) FROM Order o")
        long countTotalOrders();

        // 3️⃣ Doanh thu từ các đơn hàng đã thanh toán
        @Query(value = "SELECT COALESCE(SUM(o.total), 0) FROM dbo.orders o", nativeQuery = true)
        Double getTotalRevenue();

        // 4️⃣ Hàng tồn kho (Tổng số lượng sản phẩm còn trong kho)
        @Query("SELECT SUM(p.quantity) FROM Product p")
        Long getTotalStock();

        // 5️⃣ Sản phẩm bán chạy nhất (Sắp xếp theo tổng số lượng bán)
        @Query(value = "SELECT p.name, SUM(od.quantity) "
                        + "FROM dbo.order_details od "
                        + "JOIN dbo.products p ON od.product_id = p.id "
                        + "GROUP BY p.name "
                        + "ORDER BY SUM(od.quantity) DESC", nativeQuery = true)
        List<Object[]> getBestSellingProducts();

        // 6️⃣ Doanh thu theo tháng trong năm
        @Query("SELECT FUNCTION('MONTH', o.createdDate) as month, SUM(o.total) "
                        + "FROM Order o WHERE FUNCTION('YEAR', o.createdDate) = :year "
                        + "GROUP BY FUNCTION('MONTH', o.createdDate) ORDER BY month")
        List<Object[]> getMonthlyRevenue(@Param("year") int year);

        // 7️⃣ Doanh thu trong khoảng ngày (đã sửa kiểu Date)
        @Query("SELECT SUM(o.total) FROM Order o WHERE o.createdDate BETWEEN :startDate AND :endDate")
        Double getRevenueBetweenDates(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

        // 8. Danh sách đơn hàng (từ query bạn viết ở repository)
        @Query(value = "SELECT o.code, a.full_name, SUM(od.quantity), o.total, o.created_date " +
                        "FROM dbo.orders o " +
                        "JOIN dbo.accounts a ON o.account_id = a.id " +
                        "JOIN dbo.order_details od ON o.id = od.order_id " +
                        "GROUP BY o.code, a.full_name, o.total, o.created_date " +
                        "ORDER BY o.created_date DESC", nativeQuery = true)
        List<Object[]> getOrderSummaries();

        // 9 Thống kê số lượng đơn hàng trong khoảng ngày
        @Query("SELECT COUNT(o) FROM Order o WHERE o.createdDate BETWEEN :startDate AND :endDate")
        Long countOrdersBetweenDates(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

        @Query(value = "SELECT CAST(o.created_date AS DATE) as orderDate, COUNT(*) as totalOrders " +
                        "FROM orders o " +
                        "WHERE o.created_date BETWEEN :startDate AND :endDate " +
                        "GROUP BY CAST(o.created_date AS DATE) " +
                        "ORDER BY orderDate", nativeQuery = true)
        List<Object[]> countOrdersPerDayBetweenDates(@Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        // 🔟 Thống kê số lượng sản phẩm đã bán theo ngày trong khoảng thời gian
        @Query(value = "SELECT CAST(o.created_date AS DATE) as orderDate, p.name, SUM(od.quantity) as totalSold " +
                        "FROM dbo.order_details od " +
                        "JOIN dbo.orders o ON od.order_id = o.id " +
                        "JOIN dbo.products p ON od.product_id = p.id " +
                        "WHERE o.created_date BETWEEN :startDate AND :endDate " +
                        "GROUP BY CAST(o.created_date AS DATE), p.name " +
                        "ORDER BY orderDate ASC, totalSold DESC", nativeQuery = true)
        List<Object[]> getProductsSoldBetweenDates(@Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        // thống kê đơn hàng theo trạng thái
        @Query(value = "SELECT o.status, COUNT(*) as totalOrders " +
                        "FROM orders o " +
                        "GROUP BY o.status", nativeQuery = true)
        List<Object[]> getOrdersByStatus();

        // thống kê doanh thu theo từng sản phẩm
        @Query(value = "SELECT p.name, SUM(od.quantity * od.price) as revenue " +
                        "FROM order_details od " +
                        "JOIN products p ON od.product_id = p.id " +
                        "GROUP BY p.name " +
                        "ORDER BY revenue DESC", nativeQuery = true)
        List<Object[]> getRevenueByProduct();

        // sản phẩm sắp hết hàn
        @Query("SELECT p FROM Product p WHERE p.quantity < :threshold ORDER BY p.quantity ASC")
        List<Product> getLowStockProducts(@Param("threshold") int threshold);

        // 1️⃣ Doanh thu hôm nay
        @Query(value = "SELECT COALESCE(SUM(o.total),0) " +
                        "FROM orders o " +
                        "WHERE CAST(o.created_date AS DATE) = CAST(GETDATE() AS DATE)", nativeQuery = true)
        Double getTodayRevenue();

        // 2️⃣ Doanh thu tuần này
        @Query(value = "SELECT COALESCE(SUM(o.total),0) " +
                        "FROM orders o " +
                        "WHERE DATEPART(WEEK, o.created_date) = DATEPART(WEEK, GETDATE()) " +
                        "AND DATEPART(YEAR, o.created_date) = DATEPART(YEAR, GETDATE())", nativeQuery = true)
        Double getThisWeekRevenue();

        // 3️⃣ Doanh thu tháng này
        @Query(value = "SELECT COALESCE(SUM(o.total),0) " +
                        "FROM orders o " +
                        "WHERE MONTH(o.created_date) = MONTH(GETDATE()) " +
                        "AND YEAR(o.created_date) = YEAR(GETDATE())", nativeQuery = true)
        Double getThisMonthRevenue();

        // 4️⃣ Top khách hàng
        @Query(value = "SELECT a.full_name, SUM(o.total) as totalSpent " +
                        "FROM orders o " +
                        "JOIN accounts a ON o.account_id = a.id " +
                        "GROUP BY a.full_name " +
                        "ORDER BY totalSpent DESC " +
                        "OFFSET 0 ROWS FETCH NEXT 5 ROWS ONLY", nativeQuery = true)
        List<Object[]> getTopCustomers();

        // Doanh thu theo tất cả khách hàng
        @Query(value = "SELECT a.full_name, SUM(o.total) as totalSpent " +
                        "FROM orders o " +
                        "JOIN accounts a ON o.account_id = a.id " +
                        "GROUP BY a.full_name " +
                        "ORDER BY totalSpent DESC", nativeQuery = true)
        List<Object[]> getRevenueByCustomer();
}
