package com.asm5.controller;

import com.asm5.model.Category;
import com.asm5.model.Product;
import com.asm5.repository.ProductRepository;
import com.asm5.service.CategoryService;
import com.asm5.service.ProductService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

   @GetMapping("/")
public String home(Model model,
                   @RequestParam(defaultValue = "1") int pageNo) {
    // Lấy danh mục
    List<Category> categories = categoryService.getAllCategories();
    model.addAttribute("categories", categories);

    // Phân trang sản phẩm
    int pageSize = 12;
    Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
    Page<Product> page = productRepository
            .findByQuantityGreaterThanAndActivedTrueOrderByIdDesc(0, pageable);
    model.addAttribute("products", page.getContent());
    model.addAttribute("currentPage", pageNo);
    model.addAttribute("totalPages", page.getTotalPages());
// Lấy sản phẩm bán chạy (Top 8)
List<Product> bestSellers = productRepository.getBestSellingProducts();
model.addAttribute("bestSellers", bestSellers);

    return "index"; // trả về view home
}

@GetMapping("/sanpham")
public String home1(Model model,
                   @RequestParam(defaultValue = "1") int pageNo) {
    // Lấy danh mục
    List<Category> categories = categoryService.getAllCategories();
    model.addAttribute("categories", categories);

    // Phân trang sản phẩm
    int pageSize = 12;
    Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
    Page<Product> page = productRepository
            .findByQuantityGreaterThanAndActivedTrueOrderByIdDesc(0, pageable);
    model.addAttribute("products", page.getContent());
    model.addAttribute("currentPage", pageNo);
    model.addAttribute("totalPages", page.getTotalPages());

    return "sanpham"; // trả về view home
}


     @GetMapping("/search")
    public String searchProducts(@RequestParam("keyword") String keyword, Model model) {
        List<Product> products = productService.searchByName(keyword);
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        return "index"; // trang hiển thị danh sách sản phẩm
    }

}
