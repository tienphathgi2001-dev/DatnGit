package com.asm5.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.asm5.model.Account;
import com.asm5.model.Address;
import com.asm5.repository.AccountRepository;
import com.asm5.repository.AddressRepository;

@Service
public class accountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AddressRepository addressRepository;

    /**
     * Tìm tài khoản theo email
     */
    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    /**
     * Tìm tài khoản theo ID
     */
    public Account findById(Integer accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    /**
     * Lấy danh sách địa chỉ theo ID tài khoản
     */
    public List<Address> getAddressesByAccountId(Integer accountId) {
        Account account = findById(accountId);
        if (account == null) {
            throw new RuntimeException("Không tìm thấy tài khoản với ID: " + accountId);
        }
        return addressRepository.findByAccount(account);
    }

    /**
     * Lấy địa chỉ mặc định theo ID tài khoản
     */
    public Optional<Address> getDefaultAddressByAccountId(Integer accountId) {
        Account account = findById(accountId);
        if (account == null) {
            return Optional.empty();
        }
        return addressRepository.findByAccount(account).stream()
                .filter(Address::getIsDefault)
                .findFirst();
    }

    /**
     * Cập nhật thông tin tài khoản (email, password, photo)
     */
    public void updateAccountById(Integer id, Account updatedInfo, MultipartFile photo) throws IOException {
        Account existingAccount = findById(id);
        if (existingAccount == null) {
            throw new RuntimeException("Không tìm thấy tài khoản với ID: " + id);
        }

        // Cập nhật thông tin
        if (updatedInfo.getEmail() != null) {
            existingAccount.setEmail(updatedInfo.getEmail().trim());
        }
        if (updatedInfo.getPassword() != null && !updatedInfo.getPassword().isEmpty()) {
            existingAccount.setPassword(encodePassword(updatedInfo.getPassword()));
        }

        // Cập nhật ảnh nếu có
        if (photo != null && !photo.isEmpty()) {
            String fileName = savePhoto(photo);
            existingAccount.setAvatar(fileName);
        }

        accountRepository.save(existingAccount);
    }

    /**
     * Lưu ảnh lên server và trả về tên file
     */
    private String savePhoto(MultipartFile photo) throws IOException {
        String uploadDir = "src/main/resources/static/img/";
        String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename().replaceAll("\\s+", "");
        Path filePath = Paths.get(uploadDir + fileName);

        Files.createDirectories(filePath.getParent());
        Files.write(filePath, photo.getBytes());

        return fileName;
    }

    /**
     * Mã hóa mật khẩu (nên dùng BCrypt thật trong thực tế)
     */
    private String encodePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        return password; // TODO: Thay bằng BCryptEncoder
    }

    /**
     * Tìm tất cả các tài khoản
     */
    public List<Account> findAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Lưu tài khoản mới
     */
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    /**
     * Xóa tài khoản theo ID
     */
    public void deleteAccountById(int id) {
        accountRepository.deleteById(id);
    }
}
