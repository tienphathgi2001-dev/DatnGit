package com.asm5.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.asm5.model.News;

public interface NewsRepository extends JpaRepository<News, Long> {

    List<News> findByTitleContainingIgnoreCase(String keyword);

    org.springframework.data.domain.Page<News> findByTitleContainingIgnoreCase(String q, Pageable pageable);

    // ✅ Thêm method đếm tổng tin tức
    default long countAllNews() {
        return count(); // count() là method sẵn có của JpaRepository
    }
}
