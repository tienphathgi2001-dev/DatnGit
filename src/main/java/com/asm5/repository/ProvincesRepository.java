package com.asm5.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asm5.model.Province;

@Repository
public interface ProvincesRepository extends JpaRepository<Province, Integer> {
}

