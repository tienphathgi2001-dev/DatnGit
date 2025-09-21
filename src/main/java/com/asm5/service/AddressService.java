package com.asm5.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asm5.model.Address;
import com.asm5.model.Account;
import com.asm5.model.Ward;
import com.asm5.model.District;
import com.asm5.model.Province;
import com.asm5.repository.WardRepository;
import com.asm5.repository.DistrictRepository;
import com.asm5.repository.ProvincesRepository;
import com.asm5.repository.AddressRepository;
import com.asm5.repository.AccountRepository;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private ProvincesRepository provincesRepository;

    @Autowired
    private AccountRepository accountRepository;

    // ✅ Lưu địa chỉ
    @Transactional
public Address saveAddress(Address address) {
    if (address.getId() != null && addressRepository.existsById(address.getId())) {
        // Cập nhật địa chỉ
        Address existing = addressRepository.findById(address.getId()).orElseThrow();

        // Nếu địa chỉ này được đặt mặc định, bỏ mặc định các địa chỉ khác
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            List<Address> others = addressRepository.findByAccount(existing.getAccount());
            for (Address a : others) {
                if (!a.getId().equals(existing.getId()) && Boolean.TRUE.equals(a.getIsDefault())) {
                    a.setIsDefault(false);
                    addressRepository.save(a);
                }
            }
        }

        existing.setHouseNumber(address.getHouseNumber());

        // Lấy ward thực sự từ db để tránh detached entity
        Ward ward = wardRepository.findById(address.getWard().getId()).orElse(null);
        if (ward != null) {
            existing.setWard(ward);
        }

        existing.setIsDefault(address.getIsDefault());

        return addressRepository.save(existing);
    } else {
        // Thêm mới địa chỉ
        // Nếu là mặc định, bỏ mặc định các địa chỉ khác
        if (Boolean.TRUE.equals(address.getIsDefault()) && address.getAccount() != null) {
            List<Address> others = addressRepository.findByAccount(address.getAccount());
            for (Address a : others) {
                if (Boolean.TRUE.equals(a.getIsDefault())) {
                    a.setIsDefault(false);
                    addressRepository.save(a);
                }
            }
        }

        // Set ward thực tế
        Ward ward = wardRepository.findById(address.getWard().getId()).orElse(null);
        if (ward != null) {
            address.setWard(ward);
        }

        address.setId(null); // đảm bảo thêm mới

        return addressRepository.save(address);
    }
    
}



   
    

    // ✅ Lấy danh sách địa chỉ theo ID tài khoản
    @Transactional(readOnly = true)
    public List<Address> getAddressesByAccountId(Integer accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy tài khoản với ID: " + accountId);
        }
        return addressRepository.findByAccountWithFullAddress(accountOpt.get());
    }
  
    
    @Transactional(readOnly = true)
    public Optional<Address> getDefaultAddressByAccountId(Integer accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) return Optional.empty();
        return addressRepository.findByAccountAndIsDefaultTrue(accountOpt.get());
    }
     @Transactional(readOnly = true)
    public Optional<Address> findById(Integer id) {
        return addressRepository.findById(id);
    }
}
