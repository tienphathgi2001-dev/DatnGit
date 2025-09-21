package com.asm5.controller;

import com.asm5.model.Ward;
import com.asm5.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/wards")
public class WardRestController {

    @Autowired
    private WardRepository wardRepository;

    @GetMapping("/by-district/{districtId}")
    public List<Ward> getWardsByDistrict(@PathVariable Integer districtId) {
        return wardRepository.findByDistrict_Id(districtId);
    }
}

