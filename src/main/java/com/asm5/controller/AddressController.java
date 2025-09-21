package com.asm5.controller;

import com.asm5.model.Account;
import com.asm5.model.Address;
import com.asm5.model.Ward;
import com.asm5.repository.AddressRepository;
import com.asm5.repository.DistrictRepository;
import com.asm5.repository.ProvincesRepository;
import com.asm5.repository.WardRepository;
import com.asm5.service.AddressService;
import com.asm5.service.accountService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private accountService accountService;

    @Autowired
    private ProvincesRepository provincesRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private WardRepository wardRepository;

  @GetMapping("/list")
public String showAddressList(HttpSession session, Model model) {
    Integer accountId = (Integer) session.getAttribute("accountId");
    
    // Nếu chưa đăng nhập thì về login
    if (accountId == null) {
        return "redirect:/login";
    }

    Account account = accountService.findById(accountId);
    if (account == null) {
        session.invalidate(); // clear session nếu account không tồn tại
        return "redirect:/login";
    }

    List<Address> addresses = addressService.getAddressesByAccountId(accountId);
    model.addAttribute("account", account);
    model.addAttribute("addresses", addresses != null ? addresses : new ArrayList<>());
    return "address/list";
}


    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddress(@PathVariable Integer id, HttpSession session) {
        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return ResponseEntity.status(401).build(); // chưa đăng nhập
        }
        Address address = addressRepository.findById(id).orElse(null);
        if (address == null) {
            return ResponseEntity.notFound().build();
        }
        if (!address.getAccount().getId().equals(accountId)) {
            return ResponseEntity.status(403).build(); // không phải chủ sở hữu
        }
        return ResponseEntity.ok(address);
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model) {
        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return "redirect:/login";
        }

        model.addAttribute("address", new Address());
        model.addAttribute("provinces", provincesRepository.findAll());
        model.addAttribute("districts", districtRepository.findAll());
        model.addAttribute("wards", wardRepository.findAll());

        return "address/add"; // dùng chung form add & edit
    }

    @PostMapping("/add")
    public String addSubmit(@ModelAttribute Address address, HttpSession session) {
        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return "redirect:/login";
        }
        Account account = accountService.findById(accountId);
        if (account == null) {
            return "redirect:/login";
        }
        address.setAccount(account);
        addressService.saveAddress(address);
        return "redirect:/address/list";
    }

   @GetMapping("/edit/{id}")
public String editForm(@PathVariable Integer id, HttpSession session, Model model) {
    Integer accountId = (Integer) session.getAttribute("accountId");
    if (accountId == null) {
        return "redirect:/login";
    }
    Address address = addressRepository.findById(id).orElse(null);
    if (address == null || !address.getAccount().getId().equals(accountId)) {
        return "redirect:/address/list";
    }

    model.addAttribute("address", address);

    // Đẩy tất cả tỉnh
    model.addAttribute("provinces", provincesRepository.findAll());

    // Đẩy danh sách huyện thuộc tỉnh của địa chỉ đang sửa
    if (address.getWard() != null && address.getWard().getDistrict() != null) {
        Integer provinceId = address.getWard().getDistrict().getProvince().getId();
        model.addAttribute("districts", districtRepository.findByProvince_Id(provinceId));
    } else {
        model.addAttribute("districts", districtRepository.findAll());
    }

    // Đẩy danh sách xã thuộc huyện của địa chỉ đang sửa
    if (address.getWard() != null && address.getWard().getDistrict() != null) {
        Integer districtId = address.getWard().getDistrict().getId();
        model.addAttribute("wards", wardRepository.findByDistrict_Id(districtId));
    } else {
        model.addAttribute("wards", wardRepository.findAll());
    }

    return "address/edit";
}

@PostMapping("/edit/{id}")
public String editSubmit(
        @PathVariable Integer id,
        @RequestParam String houseNumber,
        @RequestParam Integer wardId,
        @RequestParam(required = false) Boolean isDefault,
        HttpSession session) {
    
    Integer accountId = (Integer) session.getAttribute("accountId");
    if (accountId == null) {
        return "redirect:/login";
    }

    Address existing = addressRepository.findById(id).orElse(null);
    if (existing == null || !existing.getAccount().getId().equals(accountId)) {
        return "redirect:/address/list";
    }

    existing.setHouseNumber(houseNumber);

    Ward ward = wardRepository.findById(wardId).orElse(null);
    if (ward != null) {
        existing.setWard(ward);
    }

    existing.setIsDefault(isDefault != null && isDefault);

    addressService.saveAddress(existing);

    return "redirect:/address/list";
}

    @PostMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Integer id, HttpSession session) {
        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return "redirect:/login";
        }
        Address address = addressRepository.findById(id).orElse(null);
        if (address != null && address.getAccount().getId().equals(accountId)) {
            addressRepository.delete(address);
        }
        return "redirect:/address/list";
    }
}
