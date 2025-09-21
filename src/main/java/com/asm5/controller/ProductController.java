package com.asm5.controller;

import com.asm5.model.Account;
import com.asm5.model.Product;
import com.asm5.model.Review;
import com.asm5.repository.AccountRepository;
import com.asm5.repository.CategoryRepository;
import com.asm5.repository.ProductRepository;
import com.asm5.repository.ReviewRepository;
import com.asm5.service.ProductService;
import com.asm5.service.ReviewService;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ProductController {
@Autowired
private ReviewRepository reviewRepository;

@Autowired 
private AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ReviewService reviewService;
    private final CategoryRepository categoryRepository;

    // Constructor injection
    public ProductController(ProductRepository productRepository,
                             ProductService productService,
                             ReviewService reviewService,
                             CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productService = productService;
        this.reviewService = reviewService;
        this.categoryRepository = categoryRepository;
    }

    // Trang hiển thị tất cả sản phẩm
    @RequestMapping("/sanpham")
    public String getProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products); // ✅ đồng nhất tên attribute
        return "sanpham"; // file: sanpham.html
    }




    // Tìm kiếm theo giá
    @GetMapping("/products")
    public String showProducts(@RequestParam(name = "minPrice", required = false) Integer minPrice,
                               @RequestParam(name = "maxPrice", required = false) Integer maxPrice,
                               Model model) {
        List<Product> products;

        if (minPrice != null && maxPrice != null) {
            products = productService.searchProductsByPrice(minPrice, maxPrice);
        } else {
            products = productService.getAllProducts();
        }

        model.addAttribute("products", products); // ✅ đổi về "products"
        return "sanpham"; // file: sanpham.html
    }
    
     @GetMapping("/product/{id}")
    public String getProductDetail(@PathVariable Integer id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/sanpham";
        }

          List<Review> reviews = reviewService.getReviewsByProduct(id);
    model.addAttribute("product", product);
    model.addAttribute("reviews", reviews);
    model.addAttribute("newReview", new Review());

        return "chitietsanpham"; // file html
    }

    // Thêm review
 @PostMapping("/product/{id}/review")
public String addReview(@PathVariable("id") Integer id,
                        @ModelAttribute Review newReview,
                        HttpSession session,
                        RedirectAttributes ra) {
    Account user = (Account) session.getAttribute("user");
    if (user == null) {
        ra.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để đánh giá.");
        return "redirect:/login";
    }

    Product p = productService.getProductById(id);
    if (p == null) {
        ra.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại.");
        return "redirect:/sanpham";
    }

    if (newReview.getComment() == null || newReview.getComment().trim().isEmpty()) {
        ra.addFlashAttribute("errorMessage", "Nội dung đánh giá không được trống.");
        return "redirect:/product/" + id;
    }

    newReview.setProduct(p);
    newReview.setAccount(user);
    reviewRepository.save(newReview);

    ra.addFlashAttribute("successMessage", "Đã gửi đánh giá.");
    return "redirect:/product/" + id + "#reviews";
}


}
