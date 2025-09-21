package com.asm5.service;

import com.asm5.model.Product;
import com.asm5.repository.ProductRepository;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Product getProductById(Integer id) {
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null); // Nếu không tìm thấy, trả về null
    }
    
    // Tìm sản phẩm theo khoảng giá
    // public List<Product> searchProductsByPrice(int minPrice, int maxPrice) {
    //     return productRepository.findByPriceBetween(minPrice, maxPrice);
    // }
    public Product findById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> searchProductsByPrice(Integer minPrice, Integer maxPrice) {
        return productRepository.findByPriceBetweenAndQuantityGreaterThanAndActivedTrueOrderByIdDesc(
                minPrice, maxPrice, 0);
    }
    public List<Product> getProductsByCategory(int categoryId) {
    return productRepository.findByCategoryId(categoryId);
}
  public List<Product> searchByName(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }
    

}
