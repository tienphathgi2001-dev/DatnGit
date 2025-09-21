package com.asm5.repository;

import com.asm5.model.Product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // 5️⃣ Tìm kiếm theo tên, không phân biệt hoa thường, còn hàng, active, phân
    // trang
    Page<Product> findByNameContainingIgnoreCaseAndQuantityGreaterThanAndActivedTrue(
            String name, int quantity, Pageable pageable);

              // Tìm sản phẩm theo khoảng giá, còn hàng và active
    List<Product> findByPriceBetweenAndQuantityGreaterThanAndActivedTrueOrderByIdDesc(
            Integer minPrice, Integer maxPrice, int quantity);

    // 1️⃣ Lấy tất cả sản phẩm active, còn hàng, phân trang
    Page<Product> findByQuantityGreaterThanAndActivedTrueOrderByIdDesc(int quantity, Pageable pageable);

    // 2️⃣ Lọc theo categoryId
    Page<Product> findByCategoryIdAndQuantityGreaterThanAndActivedTrueOrderByIdDesc(Integer categoryId, int quantity,
            Pageable pageable);

    // 3️⃣ Lọc theo giá
    Page<Product> findByPriceBetweenAndQuantityGreaterThanAndActivedTrueOrderByIdDesc(int minPrice, int maxPrice,
            int quantity, Pageable pageable);

    // 4️⃣ Lọc theo category + giá
    Page<Product> findByCategoryIdAndPriceBetweenAndQuantityGreaterThanAndActivedTrueOrderByIdDesc(Integer categoryId,
            int minPrice, int maxPrice, int quantity, Pageable pageable);

            List<Product> findByCategoryId(Integer categoryId);

            List<Product> findByNameContainingIgnoreCase(String name);

            Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);
@Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Integer categoryId);

    @Query(value = "SELECT p.* " +
               "FROM dbo.products p " +
               "JOIN ( " +
               "   SELECT od.product_id, SUM(od.quantity) AS total_sold " +
               "   FROM dbo.order_details od " +
               "   GROUP BY od.product_id " +
               ") t ON p.id = t.product_id " +
               "ORDER BY t.total_sold DESC",
       nativeQuery = true)
List<Product> getBestSellingProducts();


}
