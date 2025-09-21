package com.asm5.controller.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.asm5.model.Account;
import com.asm5.repository.AccountRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private AccountRepository accountRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email,
                                   @RequestParam String password,
                                   HttpSession session) {
        Account account = accountRepository.findByEmail(email);

        if (account == null || !account.getPassword().equals(password) || !account.getActivated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Đăng nhập thất bại. Kiểm tra lại thông tin.");
        }

        // Lưu session
        session.setAttribute("user", account);

        return ResponseEntity.ok(account);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String email,
                                      @RequestParam String password,
                                      @RequestParam String cfmpassword,
                                      @RequestParam String fullname) {

        if (!password.equals(cfmpassword)) {
            return ResponseEntity.badRequest().body("Mật khẩu không khớp");
        }
        if (email == null || email.trim().isEmpty() || fullname == null || fullname.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email và họ tên không được để trống");
        }
        if (accountRepository.findByEmail(email) != null) {
            return ResponseEntity.badRequest().body("Email đã được sử dụng");
        }

        Account account = new Account();
        account.setUserName(email);
        account.setEmail(email);
        account.setPassword(password);
        account.setFullName(fullname);
        account.setActivated(true);
        account.setAdmin(false);

        accountRepository.save(account);

        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Đăng xuất thành công");
    }
}
