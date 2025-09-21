package com.asm5.service;

import com.asm5.model.Account;
import com.asm5.model.CartDetail;
import com.asm5.model.CartDetailId;
import com.asm5.model.Product;
import com.asm5.repository.AccountRepository;
import com.asm5.repository.CartDetailRepository;
import com.asm5.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartDetailService {

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AccountRepository accountRepository;

    // Thêm sản phẩm vào giỏ hàng
    public void addToCart(Integer accountId, Integer productId, Integer quantity) {
        if (accountId == null) {
            throw new IllegalArgumentException("User must be logged in to add items to cart.");
        }
        if (productId == null || quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid product ID or quantity.");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        CartDetail cartDetail = cartDetailRepository.findByAccountAndProduct(account, product);
        if (cartDetail != null) {
            cartDetail.setQuantity(cartDetail.getQuantity() + quantity);
        } else {
            cartDetail = new CartDetail();
            cartDetail.setAccount(account);
            cartDetail.setProduct(product);
            cartDetail.setQuantity(quantity);
        }

        cartDetailRepository.save(cartDetail);
    }

    // Lấy giỏ hàng của người dùng
    public List<CartDetail> getCartDetailsByAccount(Integer accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));
        return cartDetailRepository.findByAccount(account);
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    public void updateCart(Integer accountId, Integer productId, String action) {
        CartDetailId id = new CartDetailId(accountId, productId);
        CartDetail cartDetail = cartDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        int current = cartDetail.getQuantity();
        if ("increase".equals(action)) {
            cartDetail.setQuantity(current + 1);
        } else if ("decrease".equals(action) && current > 1) {
            cartDetail.setQuantity(current - 1);
        } else {
            cartDetailRepository.deleteById(id);
            return;
        }
        cartDetailRepository.save(cartDetail);
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public void removeCartDetail(Integer accountId, Integer productId) {
        CartDetailId id = new CartDetailId(accountId, productId);
        cartDetailRepository.deleteById(id);
    }

    // Tìm cart detail theo accountId và productId
    public CartDetail findById(Integer accountId, Integer productId) {
        CartDetailId id = new CartDetailId(accountId, productId);
        return cartDetailRepository.findById(id).orElse(null);
    }

    // Tìm cart detail theo CartDetailId (overload)
    public CartDetail findById(CartDetailId id) {
        return cartDetailRepository.findById(id).orElse(null);
    }

    public void save(CartDetail cartDetail) {
        cartDetailRepository.save(cartDetail);
    }

    public void delete(CartDetail cartDetail) {
        cartDetailRepository.delete(cartDetail);
    }

    @Transactional
    public void deleteAllByAccountId(Integer accountId) {
        cartDetailRepository.deleteByAccountId(accountId);
    }
     public List<CartDetail> getCartDetailsByAccountId(Integer accountId) {
        return cartDetailRepository.findByAccountId(accountId);
    }
}
