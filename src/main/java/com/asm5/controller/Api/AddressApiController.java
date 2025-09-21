package com.asm5.controller.Api;

import com.asm5.model.Account;
import com.asm5.model.Address;
import com.asm5.model.Ward;
import com.asm5.repository.AddressRepository;
import com.asm5.repository.WardRepository;
import com.asm5.service.AddressService;
import com.asm5.service.accountService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressApiController {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private accountService accountService;

    @Autowired
    private WardRepository wardRepository;

    // ✅ Lấy danh sách địa chỉ của user đang đăng nhập
    @GetMapping
    public ResponseEntity<?> getAddresses(HttpSession session) {
        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        List<Address> addresses = addressService.getAddressesByAccountId(accountId);
        return ResponseEntity.ok(addresses);
    }
    @GetMapping("/api/list")
@ResponseBody
public ResponseEntity<List<Address>> getUserAddresses(HttpSession session) {
    Integer accountId = (Integer) session.getAttribute("accountId");
    if (accountId == null) {
        return ResponseEntity.status(401).build();
    }

    List<Address> addresses = addressService.getAddressesByAccountId(accountId);
    return ResponseEntity.ok(addresses);
}


    // ✅ Lấy 1 địa chỉ theo id
    @GetMapping("/{id}")
    public ResponseEntity<?> getAddress(@PathVariable Integer id, HttpSession session) {
        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        Address address = addressRepository.findById(id).orElse(null);
        if (address == null) {
            return ResponseEntity.notFound().build();
        }
        if (!address.getAccount().getId().equals(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền");
        }
        return ResponseEntity.ok(address);
    }

    // ✅ Thêm mới địa chỉ
    @PostMapping
    public ResponseEntity<?> addAddress(@RequestBody Address address, HttpSession session) {
        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        Account account = accountService.findById(accountId);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không tìm thấy account");
        }
        address.setAccount(account);
        Address saved = addressService.saveAddress(address);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ✅ Cập nhật địa chỉ
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(
            @PathVariable Integer id,
            @RequestBody Address request,
            HttpSession session) {
        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }

        Address existing = addressRepository.findById(id).orElse(null);
        if (existing == null || !existing.getAccount().getId().equals(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền sửa địa chỉ này");
        }

        existing.setHouseNumber(request.getHouseNumber());
        if (request.getWard() != null) {
            Ward ward = wardRepository.findById(request.getWard().getId()).orElse(null);
            if (ward != null) {
                existing.setWard(ward);
            }
        }
        existing.setIsDefault(request.getIsDefault());

        Address updated = addressService.saveAddress(existing);
        return ResponseEntity.ok(updated);
    }

    // ✅ Xoá địa chỉ
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Integer id, HttpSession session) {
        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }
        Address address = addressRepository.findById(id).orElse(null);
        if (address == null) {
            return ResponseEntity.notFound().build();
        }
        if (!address.getAccount().getId().equals(accountId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Không có quyền xoá");
        }

        addressRepository.delete(address);
        return ResponseEntity.ok("Đã xoá địa chỉ id = " + id);
    }
}
