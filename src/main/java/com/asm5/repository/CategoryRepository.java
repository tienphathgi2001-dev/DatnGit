package com.asm5.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asm5.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer>{
	public Category findByName(String name); // ✅ nếu chỉ có "name"

}
