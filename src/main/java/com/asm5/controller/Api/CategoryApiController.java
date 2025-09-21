package com.asm5.controller.Api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.asm5.model.Category;
import com.asm5.repository.CategoryRepository;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryApiController {

    @Autowired
    private CategoryRepository categoryRepository;

    // Lấy tất cả categories (không phân trang)
    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        List<Category> list = categoryRepository.findAll();
        return ResponseEntity.ok(list);
    }

    // Lấy danh sách có phân trang
    @GetMapping("/page/{pageNo}")
    public ResponseEntity<Page<Category>> getPage(@PathVariable("pageNo") int pageNo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(pageNo, 3, sort);
        Page<Category> page = categoryRepository.findAll(pageable);
        return ResponseEntity.ok(page);
    }

    // Lấy chi tiết theo id
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable("id") Integer id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Thêm mới
    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category) {
        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(saved);
    }

    // Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable("id") Integer id, @RequestBody Category category) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    existing.setName(category.getName());
                    // set thêm các field khác nếu có
                    Category updated = categoryRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Xoá
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
