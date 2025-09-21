package com.asm5.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.asm5.model.Category;
import com.asm5.repository.CategoryRepository;
import com.asm5.repository.ProductRepository;

@Controller
@RequestMapping("/admin/category")
public class CategoryManagementController {

    @Autowired
    private CategoryRepository categoryRepository;


    @Autowired
    private ProductRepository productRepository;


    // Danh sách không phân trang
    @GetMapping("/list")
    public String list(Model model) {
        List<Category> list = categoryRepository.findAll();
          Map<Integer, Long> productCounts = new HashMap<>();
    for (Category c : list) {
        productCounts.put(c.getId(), productRepository.countByCategoryId(c.getId()));
    }
        model.addAttribute("list", list);

        model.addAttribute("productCounts", productCounts);
        return "admin/category/qlloaisanpham";
    }

    // Danh sách có phân trang
    @GetMapping("/list-page/{pageNo}")
    public String listPage(Model model, @PathVariable("pageNo") Integer pageNo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(pageNo, 3, sort);
        Page<Category> page = categoryRepository.findAll(pageable);
        model.addAttribute("list", page.getContent());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        return "admin/category/list-page";
    }

    // Hiển thị form thêm mới
    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category/add";
    }

    // Xử lý thêm mới
   @PostMapping("/insert")
public String insert(@ModelAttribute("category") Category category,
                     @RequestParam("file") MultipartFile file) {
    try {
        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            String uploadDir = "uploads/";

            // Tạo thư mục nếu chưa có
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            // Lưu file vào thư mục
            file.transferTo(new java.io.File(uploadDir + fileName));

            // Lưu tên file vào DB
            category.setImage(fileName);
        }
        categoryRepository.save(category);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return "redirect:/admin/category/list";
}

@PostMapping("/update")
public String update(@ModelAttribute("category") Category category,
                     @RequestParam("file") MultipartFile file) {
    try {
        // Nếu có upload ảnh mới
        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            String uploadDir = "uploads/";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);

            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            file.transferTo(new java.io.File(uploadDir + fileName));
            category.setImage(fileName);
        } else {
            // Nếu không upload ảnh mới thì giữ ảnh cũ
            Category oldCategory = categoryRepository.findById(category.getId()).orElse(null);
            if (oldCategory != null) {
                category.setImage(oldCategory.getImage());
            }
        }

        categoryRepository.save(category);
    } catch (Exception e) {
        e.printStackTrace();
    }

    return "redirect:/admin/category/list";
}


    // Hiển thị form chỉnh sửa
    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") Integer id) {
        Category category = categoryRepository.findById(id).orElse(null);
        model.addAttribute("category", category);
        return "admin/category/edit";
    }

    // Xử lý cập nhật
    // @PostMapping("/update")
    // public String update(@ModelAttribute("category") Category category) {
    //     categoryRepository.save(category);
    //     return "redirect:/admin/category/list";
    // }

    // Xử lý xoá
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        categoryRepository.deleteById(id);
        return "redirect:/admin/category/list";
    }
}
