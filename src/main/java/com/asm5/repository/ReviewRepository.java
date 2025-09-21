package com.asm5.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.asm5.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByProductId(Integer productId);
    List<Review> findByAccountId(Integer accountId);

    // Lấy 3 review mới nhất
    List<Review> findTop3ByProductIdOrderByCreatedAtDesc(Integer productId);

    // Lấy tất cả review theo product, sắp xếp mới nhất trước
    List<Review> findByProductIdOrderByCreatedAtDesc(Integer productId);
    
    
}
