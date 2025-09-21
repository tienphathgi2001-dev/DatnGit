package com.asm5.controller.Api;

import com.asm5.model.Account;
import com.asm5.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
public class AccountApiController {

    @Autowired
    private AccountRepository accountRepository;

    // Lấy danh sách account
    @GetMapping
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    // Lấy chi tiết theo email
    @GetMapping("/{email}")
    public ResponseEntity<Account> getAccountByEmail(@PathVariable String email) {
        Account account = accountRepository.findByEmail(email);
        if (account != null) {
            return ResponseEntity.ok(account);
        }
        return ResponseEntity.notFound().build();
    }

    // Thêm account mới (không upload ảnh ở đây, chỉ test JSON)
    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        return accountRepository.save(account);
    }

    // Cập nhật account theo email
    @PutMapping("/{email}")
    public ResponseEntity<Account> updateAccount(@PathVariable String email,
                                                 @RequestBody Account updated) {
        Account existing = accountRepository.findByEmail(email);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        existing.setFullName(updated.getFullName());
        existing.setActivated(updated.getActivated());
        existing.setAdmin(updated.getAdmin());
        // Avatar: có thể thêm logic upload file riêng cho API
        return ResponseEntity.ok(accountRepository.save(existing));
    }

    // Xóa account theo email
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String email) {
        Account account = accountRepository.findByEmail(email);
        if (account != null) {
            accountRepository.delete(account);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
