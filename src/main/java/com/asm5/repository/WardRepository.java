package com.asm5.repository;

import com.asm5.model.District;
import com.asm5.model.Ward;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WardRepository extends JpaRepository<Ward, Integer> {
	 // Nếu muốn lọc theo Huyện

    // ✅ CHỈ GIỮ cái này nếu lọc theo Tỉnh thông qua Quận/Huyện
	List<Ward> findByDistrict(District district);
    List<Ward> findByDistrict_Id(Integer districtId);

    Optional<Ward> findByMa(String ma);


}

