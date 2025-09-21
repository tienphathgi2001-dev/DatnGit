package com.asm5.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


import com.asm5.model.Account;
import com.asm5.model.Address;
import com.asm5.repository.AccountRepository;



import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping
public class ProfileController {

    @Autowired
    private AccountRepository accountRepository;

   

// ...existing code...
@GetMapping("/profile")
public String showProfile(HttpSession session, Model model) {
    Integer accountId = (Integer) session.getAttribute("accountId");
    if (accountId == null) {
        return "redirect:/login";
    }

    Account account = accountRepository.findById(accountId).orElse(null);
    if (account == null) {
        return "redirect:/login";
    }

    Address defaultAddress = null;
    List<Address> addresses = account.getAddresses();
    if (addresses != null && !addresses.isEmpty()) {
        defaultAddress = addresses.stream()
            .filter(addr -> Boolean.TRUE.equals(addr.getIsDefault()))
            .findFirst()
            .orElse(addresses.get(0));
    }
    System.out.println("Default Address: " + defaultAddress); // debug

    model.addAttribute("defaultAddress", defaultAddress);
    model.addAttribute("session", session);

    return "profile";
}
// ...existing code...

    @PostMapping("/profile/update-photo")
    public ResponseEntity<?> updatePhoto(@RequestParam("photo") MultipartFile file, HttpSession session) {
        try {
            if (file != null && !file.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get("src/main/resources/static/img");
                Files.createDirectories(uploadPath);
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                session.setAttribute("photo", fileName);

                Integer accountId = (Integer) session.getAttribute("accountId");
                if (accountId != null) {
                    Account account = accountRepository.findById(accountId).orElse(null);
                    if (account != null) {
                        account.setAvatar(fileName);
                        accountRepository.save(account);
                    }
                }

                return ResponseEntity.ok("Uploaded");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed");
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/profile/change-password")
    public String showChangePassword(HttpSession session, Model model) {
        if (session.getAttribute("accountId") == null) return "redirect:/taikhoan";
        return "change-password";
    }

    @PostMapping("/profile/change-password")
    public String processChangePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer accountId = (Integer) session.getAttribute("accountId");
        if (accountId == null) return "redirect:/taikhoan";

        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản không tồn tại!");
            return "redirect:/profile/change-password";
        }

        if (!account.getPassword().equals(oldPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu cũ không đúng!");
            return "redirect:/profile/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận không khớp!");
            return "redirect:/profile/change-password";
        }

        account.setPassword(newPassword);
        accountRepository.save(account);

        redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công!");
        return "redirect:/profile";
    }

 @GetMapping("/profile/edit")
public String showEditProfile(Model model, HttpSession session) {
    Integer accountId = (Integer) session.getAttribute("accountId");
    if (accountId == null) return "redirect:/taikhoan";

    Account account = accountRepository.findById(accountId).orElse(null);
    if (account == null) return "redirect:/taikhoan";

    model.addAttribute("account", account);
    return "profile_edit";
}


@PostMapping("/profile/edit")
public String updateProfile(@RequestParam String email,
                            @RequestParam String fullName,
                            HttpSession session,RedirectAttributes redirectAttributes) {

    Integer accountId = (Integer) session.getAttribute("accountId");
    Account account = accountRepository.findById(accountId).orElse(null);
    if (account == null) return "redirect:/login";

    account.setEmail(email);
    account.setFullName(fullName); // cập nhật tên

    accountRepository.save(account);

    redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");

    return "redirect:/profile";
}

// ...existing code...




}
