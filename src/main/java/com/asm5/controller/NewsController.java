package com.asm5.controller;


import com.asm5.model.Account;
import com.asm5.model.Comment;
import com.asm5.model.News;
import com.asm5.repository.CommentRepository;
import com.asm5.repository.NewsRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/news")
public class NewsController {

    private final NewsRepository newsRepository;
    private final CommentRepository commentRepository;

    public NewsController(NewsRepository newsRepository, CommentRepository commentRepository) {
        this.newsRepository = newsRepository;
        this.commentRepository = commentRepository;
    }

    // Danh sách + tìm kiếm
    @GetMapping({"", "/", "/list"})
public String list(@RequestParam(value = "q", required = false) String q,
                   @RequestParam(defaultValue = "1") int page,
                   @RequestParam(defaultValue = "4") int size, // mỗi trang 4 tin
                   Model model) {

    // PageRequest index bắt đầu từ 0
    Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<News> newsPage;

    if (q != null && !q.trim().isEmpty()) {
        newsPage = newsRepository.findByTitleContainingIgnoreCase(q.trim(), pageable);
    } else {
        newsPage = newsRepository.findAll(pageable);
    }

    model.addAttribute("q", q);
    model.addAttribute("newsPage", newsPage);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", newsPage.getTotalPages()); // thêm totalPages để phân trang
    return "news/list";
}


    // Chi tiết
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        News n = newsRepository.findById(id).orElse(null);
        if (n == null) {
            ra.addFlashAttribute("errorMessage", "Bài viết không tồn tại.");
            return "redirect:/news";
        }
        model.addAttribute("news", n);
        model.addAttribute("comments", commentRepository.findByNewsIdOrderByCreatedAtAsc(id));
        model.addAttribute("newComment", new Comment());
        return "news/news_detail";
    }

    // Thêm bình luận (yêu cầu login)
    @PostMapping("/{id}/comment")
    public String comment(@PathVariable Long id,
                          @ModelAttribute("newComment") Comment newComment,
                          HttpSession session,
                          RedirectAttributes ra) {
        Account user = (Account) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để bình luận.");
            return "redirect:/login";
        }

        News n = newsRepository.findById(id).orElse(null);
        if (n == null) {
            ra.addFlashAttribute("errorMessage", "Bài viết không tồn tại.");
            return "redirect:/news";
        }

        if (newComment.getContent() == null || newComment.getContent().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Nội dung bình luận không được trống.");
            return "redirect:/news/" + id;
        }

        newComment.setNews(n);
        newComment.setAccount(user);
        commentRepository.save(newComment);

        ra.addFlashAttribute("successMessage", "Đã gửi bình luận.");
        return "redirect:/news/" + id + "#comments";
    }
}

