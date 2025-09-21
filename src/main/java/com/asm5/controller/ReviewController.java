package com.asm5.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.asm5.model.Account;
import com.asm5.model.Product;
import com.asm5.model.Review;
import com.asm5.repository.AccountRepository;
import com.asm5.repository.ProductRepository;
import com.asm5.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    // Form submit từ Thymeleaf (nếu không dùng AJAX)
    // @PostMapping("/add")
    // public String addReview(@RequestParam("productId") Integer productId,
    //                         @RequestParam("accountId") Integer accountId,
    //                         @RequestParam("rating") int rating,
    //                         @RequestParam("comment") String comment,
    //                         RedirectAttributes redirectAttributes) {

    //     Review review = new Review();
    //     review.setRating(rating);
    //     review.setComment(comment);

    //     Product product = productRepository.findById(productId).orElse(null);
    //     review.setProduct(product);

    //     Account account = accountRepository.findById(accountId).orElse(null);
    //     review.setAccount(account);

    //     reviewService.saveReview(review);

    //     redirectAttributes.addFlashAttribute("success", "Cảm ơn bạn đã đánh giá!");
    //     return "redirect:/product/" + productId;
    // }
}

