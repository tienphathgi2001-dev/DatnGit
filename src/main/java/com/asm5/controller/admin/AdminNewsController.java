package com.asm5.controller.admin;

import com.asm5.model.Account;
import com.asm5.model.News;
import com.asm5.repository.NewsRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;

@Controller
@RequestMapping("/admin/news")
public class AdminNewsController {

    private final NewsRepository newsRepository;

    public AdminNewsController(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Value("${upload.news.dir:src/main/resources/static/uploads/news/}")
    private String UPLOAD_DIR;

    private boolean isAdmin(HttpSession session) {
        Boolean admin = (Boolean) session.getAttribute("admin");
        if (Boolean.TRUE.equals(admin)) return true;
        Account u = (Account) session.getAttribute("user");
        return u != null && "ADMIN".equalsIgnoreCase(u.getRole());
    }

    // Danh sách tin (phân trang)
    @GetMapping({"", "/list"})
public String list(@RequestParam(defaultValue = "1") int page,
                   @RequestParam(defaultValue = "10") int size,
                   Model model,
                   HttpSession session,
                   RedirectAttributes ra) {
    if (!isAdmin(session)) {
        ra.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập trang này.");
        return "redirect:/login";
    }
    Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<News> newsPage = newsRepository.findAll(pageable);
    model.addAttribute("newsPage", newsPage);
    model.addAttribute("currentPage", newsPage.getNumber()); // 0-based
    model.addAttribute("totalPages", newsPage.getTotalPages());
    return "admin/news/list";
}


    // Mở form thêm
    @GetMapping("/add")
    public String addForm(Model model, HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) {
            ra.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập trang này.");
            return "redirect:/login";
        }
        model.addAttribute("news", new News());
        return "admin/news/form";
    }

    // Lưu tin mới
    @PostMapping("/save")
    public String save(@ModelAttribute("news") News news,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       HttpSession session,
                       RedirectAttributes ra) {
        if (!isAdmin(session)) {
            ra.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện thao tác này.");
            return "redirect:/login";
        }
        Account author = (Account) session.getAttribute("user");
        if (author == null) {
            ra.addFlashAttribute("errorMessage", "Vui lòng đăng nhập lại.");
            return "redirect:/login";
        }

        // upload ảnh nếu có
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                news.setImage("/uploads/news/" + fileName); // đường dẫn để hiển thị
            } catch (IOException e) {
                ra.addFlashAttribute("errorMessage", "Upload ảnh thất bại: " + e.getMessage());
                return "redirect:/admin/news/add";
            }
        }

        news.setAuthor(author);
        newsRepository.save(news);
        ra.addFlashAttribute("successMessage", "Đăng tin thành công.");
        return "redirect:/admin/news/list";
    }

    // Mở form sửa
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model,
                           HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) {
            ra.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập trang này.");
            return "redirect:/login";
        }
        News n = newsRepository.findById(id).orElse(null);
        if (n == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy bài viết.");
            return "redirect:/admin/news/list";
        }
        model.addAttribute("news", n);
        return "admin/news/edit";
    }

    // Cập nhật
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("news") News form,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         HttpSession session,
                         RedirectAttributes ra) {
        if (!isAdmin(session)) {
            ra.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện thao tác này.");
            return "redirect:/login";
        }
        News n = newsRepository.findById(id).orElse(null);
        if (n == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy bài viết.");
            return "redirect:/admin/news/list";
        }

        n.setTitle(form.getTitle());
        n.setContent(form.getContent());

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                Files.copy(imageFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                n.setImage("/uploads/news/" + fileName);
            } catch (IOException e) {
                ra.addFlashAttribute("errorMessage", "Upload ảnh thất bại: " + e.getMessage());
                return "redirect:/admin/news/edit/" + id;
            }
        }

        newsRepository.save(n);
        ra.addFlashAttribute("successMessage", "Cập nhật bài viết thành công.");
        return "redirect:/admin/news/list";
    }

    // Xoá
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) {
            ra.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện thao tác này.");
            return "redirect:/login";
        }
        if (!newsRepository.existsById(id)) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy bài viết.");
            return "redirect:/admin/news/list";
        }
        newsRepository.deleteById(id);
        ra.addFlashAttribute("successMessage", "Đã xoá bài viết.");
        return "redirect:/admin/news/list";
    }
}
