package com.asm5.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.asm5.service.MailService;
import com.asm5.service.MailService.Mail;

@Controller
public class MailController {

    @Autowired
    MailService mailService;

    private final String SHOP_EMAIL = "phu734643@gmail.com"; // Email của shop

    @PostMapping("/send")
    public String sendMail(
            @RequestParam("from") String from,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            RedirectAttributes redirectAttributes) {

        try {
            // Tạo nội dung email gửi đến shop
            String emailContent = "Khách hàng: " + from + "\n\nNội dung:\n" + body;

            // Tạo email object
            Mail mail = Mail.builder()
                    .from(from) // Email của khách hàng
                    .to(SHOP_EMAIL) // Email của shop
                    .subject(subject)
                    .body(emailContent)
                    .build();

            // Gửi mail ngay lập tức
            mailService.send(mail);

            // Gửi thông báo thành công
            redirectAttributes.addFlashAttribute("message", "Email đã được gửi thành công!");
        } catch (Exception e) {
            // Gửi thông báo lỗi nếu gửi mail thất bại
            redirectAttributes.addFlashAttribute("error", "Gửi email thất bại. Vui lòng thử lại!");
        }

        return "redirect:/lienhe"; // Chuyển hướng về trang liên hệ
    }
}
