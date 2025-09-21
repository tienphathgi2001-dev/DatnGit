package com.asm5.controller.Api;

import com.asm5.model.Product;
import com.asm5.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    @Autowired
    private ProductRepository productRepository;

    // ✅ Lấy danh sách sản phẩm
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ✅ Lấy sản phẩm theo id
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id " + id));
    }

    // ✅ Thêm sản phẩm mới
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        product.setId(null); // để auto-increment
        return productRepository.save(product);
    }

    // ✅ Cập nhật sản phẩm
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Integer id, @RequestBody Product product) {
        Optional<Product> existing = productRepository.findById(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("Không tìm thấy sản phẩm với id " + id);
        }
        Product p = existing.get();
        p.setName(product.getName());
        p.setPrice(product.getPrice());
        p.setQuantity(product.getQuantity());
        p.setCategory(product.getCategory());
        p.setActived(product.getActived());
        return productRepository.save(p);
    }

    // ✅ Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("❌ Không tìm thấy sản phẩm với id = " + id);
        }

        try {
            productRepository.deleteById(id);
            return ResponseEntity.ok("✅ Đã xóa sản phẩm có id = " + id);
        } catch (DataIntegrityViolationException e) {
            // Trường hợp bị ràng buộc khóa ngoại (FK)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ Không thể xóa sản phẩm vì đang được tham chiếu!");
        }
    }

}
