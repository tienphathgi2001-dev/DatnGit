package com.asm5.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asm5.model.District;
import com.asm5.model.Province;

public interface DistrictRepository extends JpaRepository<District, Integer> {
	 List<District> findByProvince(Province province); // ✅ để hiển thị District theo Province
	    List<District> findByProvince_Id(Integer provinceId);
		Optional<District> findByCode(String code);


}

