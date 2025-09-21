package com.asm5.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asm5.model.District;
import com.asm5.repository.DistrictRepository;

@RestController
@RequestMapping("/districts")
public class DistrictRestController {

    @Autowired
    private DistrictRepository districtRepository;

    @GetMapping("/by-province/{provinceId}")
    public List<District> getDistrictsByProvince(@PathVariable("provinceId") Integer provinceId) {
        return districtRepository.findByProvince_Id(provinceId);
    }
}
