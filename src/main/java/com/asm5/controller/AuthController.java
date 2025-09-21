package com.asm5.controller;

import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.asm5.model.Account;
import com.asm5.model.Address;
import com.asm5.model.District;
import com.asm5.model.Province;
import com.asm5.model.Ward;
import com.asm5.repository.AccountRepository;
import com.asm5.model.MD5Util; // ✅ import MD5Util

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JavaMailSender mailSender;

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login1")
public String login(@RequestParam("email") String email,
                    @RequestParam("password") String password,
                    HttpSession session,
                    RedirectAttributes redirectAttributes) {

    Account account = accountRepository.findByEmail(email);
// ✅ thay vì "user"
session.setAttribute("account", account);

    if (account == null) {
        redirectAttributes.addFlashAttribute("errorMessage", "Email không tồn tại.");
        return "redirect:/login";
    }

    if (!account.getActivated()) {
        redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        return "redirect:/login";
    }

    // ✅ Kiểm tra mật khẩu
    boolean isValidPassword;
    if (Boolean.TRUE.equals(account.getAdmin())) {
        // 👉 Admin: so sánh raw password
        isValidPassword = account.getPassword().equals(password);
    } else {
        // 👉 User: so sánh MD5
        String hashedPassword = MD5Util.encrypt(password);
        isValidPassword = account.getPassword().equals(hashedPassword);
    }

    if (!isValidPassword) {
        redirectAttributes.addFlashAttribute("errorMessage", "Sai mật khẩu, vui lòng thử lại.");
        return "redirect:/login";
    }

    // ✅ Nếu qua hết check -> login thành công
    session.setAttribute("user", account);
    session.setAttribute("accountId", account.getId());
    session.setAttribute("fullName", account.getFullName());
    session.setAttribute("email", account.getEmail());
    session.setAttribute("photo", account.getAvatar());
    session.setAttribute("activated", account.getActivated());
    session.setAttribute("admin", account.getAdmin());

    // 🔽 Lấy địa chỉ mặc định nếu có
    Address defaultAddress = null;
    if (account.getAddresses() != null && !account.getAddresses().isEmpty()) {
        for (Address addr : account.getAddresses()) {
            if (Boolean.TRUE.equals(addr.getIsDefault())) {
                defaultAddress = addr;
                break;
            }
        }
        if (defaultAddress == null) {
            defaultAddress = account.getAddresses().get(0);
        }
    }

    if (defaultAddress != null) {
        Ward ward = defaultAddress.getWard();
        District district = null;
        Province province = null;
        if (ward != null) {
            district = ward.getDistrict();
            if (district != null) {
                province = district.getProvince();
            }
        }
        if (ward != null) ward.getTen();
        if (district != null) district.getName();
        if (province != null) province.getName();

        session.setAttribute("defaultAddress", defaultAddress);
    } else {
        session.setAttribute("defaultAddress", null);
    }

    redirectAttributes.addFlashAttribute("successMessage1", "Đăng nhập thành công!");
    return account.getAdmin() ? "redirect:/admin/index" : "redirect:/";
}


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/taikhoan";
    }

    @PostMapping("/register")
    public String postRegister(Model model,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("cfmpassword") String cfmpassword,
            @RequestParam("fullname") String fullname,
            RedirectAttributes redirectAttributes) {
        if (!password.equals(cfmpassword)) {
            model.addAttribute("message", "Mật khẩu không khớp");
            return "register";
        }

        if (email == null || email.trim().isEmpty() || fullname == null || fullname.trim().isEmpty()) {
            model.addAttribute("message", "Email và họ tên không được để trống");
            return "register";
        }

        if (accountRepository.findByEmail(email) != null) {
            model.addAttribute("message", "Email " + email + " đã được sử dụng");
            return "register";
        }

        Account account = new Account();
        account.setUserName(email);
        account.setEmail(email);

        // ✅ Mã hóa mật khẩu trước khi lưu
        account.setPassword(MD5Util.encrypt(password));
        account.setFullName(fullname);
        account.setActivated(true);
        account.setAdmin(false);

        try {
            accountRepository.save(account);
            return "login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi khi đăng ký. Vui lòng thử lại!");
            model.addAttribute("param", account);
            return "register";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "doimatkhau"; 
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        Account account = accountRepository.findByEmail(email);

        if (account == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email không tồn tại trong hệ thống!");
            return "redirect:/forgot-password";
        }

        String randomPassword = UUID.randomUUID().toString().substring(0, 8);

        // ✅ Mã hóa mật khẩu tạm trước khi lưu
        account.setPassword(MD5Util.encrypt(randomPassword));
        accountRepository.save(account);

        try {
            sendEmail(email, "Mật khẩu mới của bạn",
                    "Xin chào " + account.getFullName() + ",\n\n" +
                            "Mật khẩu đăng nhập tạm thời của bạn là: " + randomPassword +
                            "\nVui lòng đổi lại mật khẩu sau khi đăng nhập.\n\nTrân trọng!");
            redirectAttributes.addFlashAttribute("successMessage", "Mật khẩu tạm đã được gửi tới email của bạn!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể gửi email. Vui lòng thử lại sau.");
        }

        return "redirect:/login";
    }
}