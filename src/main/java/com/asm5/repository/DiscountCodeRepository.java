package com.asm5.repository;

import com.asm5.model.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Integer> {
    Optional<DiscountCode> findByCode(String code);
    DiscountCode findByCodeAndActiveTrue(String code);
}