package com.asm5.controller.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.asm5.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.asm5.model.Account;
import com.asm5.repository.AccountRepository;


@Controller
@RequestMapping("admin/account")
public class AccountManagementController {

    private final MailService mailService;
    @Autowired
    private AccountRepository accountRepository;

    @Value("${upload.dir:src/main/resources/static/img/}")
    private String UPLOAD_DIR;

    AccountManagementController(MailService mailService) {
        this.mailService = mailService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("photo"); // Prevent photo from binding to Account
    }

    @RequestMapping("/list/{pageNo}")
    public String list(Model model, @PathVariable("pageNo") Integer pageNo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        int adjustedPageNo = Math.max(pageNo - 1, 0);
        Pageable pageable = PageRequest.of(adjustedPageNo, 5, sort);


        Page<Account> list = accountRepository.findByRole("USER", pageable);
        // Page<Account> list = accountRepository.findAll(pageable);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("accounts", accountRepository.findAll());
        model.addAttribute("totalPages", list.getTotalPages());
        model.addAttribute("list", list.getContent());
        return "admin/account/list";
    }

    @RequestMapping("/add")
    public String add(Model model) {
        model.addAttribute("account", new Account());
        return "admin/account/add";
    }

    @PostMapping("/insert")
    public String insert(@ModelAttribute("account") Account account,
                         @RequestParam("photo") MultipartFile imageFile,
                         Model model) {
        try {
            if (!imageFile.isEmpty()) {
                String fileName = saveImage(imageFile);
                account.setAvatar(fileName);
            }
            accountRepository.save(account);
            return "redirect:/admin/account/list/1";
        } catch (IOException e) {
            model.addAttribute("error", "Failed to save account: " + e.getMessage());
            return "admin/account/add";
        }
    }

    @RequestMapping("/edit/{email}")
    public String edit(@PathVariable("email") String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        model.addAttribute("account", account);
        return "admin/account/edit";
    }

    @PostMapping("/update")
public String update(@ModelAttribute("account") Account account,
                     Model model) {
    try {
        // Tìm tài khoản hiện có theo email
        Account existingAccount = accountRepository.findByEmail(account.getEmail());
        if (existingAccount == null) {
            model.addAttribute("error", "Account not found!");
            return "admin/account/edit";
        }

        // Chỉ cho phép cập nhật trạng thái Activated
        existingAccount.setActivated(account.getActivated());

        // Lưu lại tài khoản đã cập nhật
        accountRepository.save(existingAccount);
        return "redirect:/admin/account/list/1";
    } catch (Exception e) {
        model.addAttribute("error", "Failed to update account: " + e.getMessage());
        return "admin/account/edit";
    }
}

    @RequestMapping("/delete/{email}")
	public String delete(@PathVariable("email")String email ) {
		Account account = accountRepository.findByEmail(email);
		accountRepository.delete(account);
		return "redirect:/admin/account/list/1";
	}


    private String saveImage(MultipartFile imageFile) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, imageFile.getBytes());
        return fileName;
    }

    private void deleteImage(String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete image: " + e.getMessage());
        }
    }
    @PostMapping("/toggle")
public String toggleAccount(@RequestParam("email") String email,
                            @RequestParam("activated") boolean activated) {
    Account acc = accountRepository.findByEmail(email);
    if (acc != null) {
        acc.setActivated(activated);
        accountRepository.save(acc);
        // Gửi email thông báo
       // Gửi email thông báo
        String subject = activated ? "Tài khoản đã được kích hoạt" : "Tài khoản đã bị vô hiệu hóa";
        String body = activated
            ? "<h3>Xin chào " + acc.getFullName() + ",</h3><p>Tài khoản của bạn đã được <b style='color:green'>kích hoạt</b> trở lại.</p><p>Trân trọng,<br>WebShop</p>"
            : "<h3>Xin chào " + acc.getFullName() + ",</h3><p>Tài khoản của bạn đã bị <b style='color:red'>vô hiệu hóa</b>. Vui lòng liên hệ quản trị viên nếu có thắc mắc.</p><p>Trân trọng,<br>WebShop</p>";

        mailService.send(acc.getEmail(), subject, body); 

    }
    return "redirect:/admin/account/list/1";
}

}