package com.asm5.model;

import java.io.Serializable;
import java.util.Objects;

public class CartDetailId implements Serializable {
    private Integer account;
    private Integer product;

    public CartDetailId() {
    }

    public CartDetailId(Integer account, Integer product) {
        this.account = account;
        this.product = product;
    }

    // ⚠️ Override equals và hashCode bắt buộc
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartDetailId)) return false;
        CartDetailId that = (CartDetailId) o;
        return Objects.equals(account, that.account) &&
               Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, product);
    }

    // Getters & Setters
    public Integer getAccount() {
        return account;
    }

    public void setAccount(Integer account) {
        this.account = account;
    }

    public Integer getProduct() {
        return product;
    }

    public void setProduct(Integer product) {
        this.product = product;
    }
}
