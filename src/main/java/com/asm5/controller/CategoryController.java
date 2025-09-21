package com.asm5.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.asm5.model.Category;
import com.asm5.model.Product;
import com.asm5.repository.CategoryRepository;
import com.asm5.repository.ProductRepository;
import com.asm5.service.CategoryService;
import com.asm5.service.ProductService;

@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    // Hiển thị theo danh mục + phân trang
    @GetMapping("/{id}")
    public String listByCategory(@PathVariable("id") Integer id,
                                 Model model,
                                 @RequestParam(defaultValue = "1") int pageNo) {
        Page<Product> products = productRepository.findByCategoryId(id, PageRequest.of(pageNo - 1, 12));

        model.addAttribute("products", products.getContent());
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategory", id);

        return "product/list"; // file: product/list.html
    }
}

