package com.asm5.repository;

import com.asm5.model.Account;
import com.asm5.model.CartDetail;
import com.asm5.model.CartDetailId;
import com.asm5.model.Product;

import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, CartDetailId> {
    CartDetail findByAccountAndProduct(Account account, Product product);

    List<CartDetail> findByAccount(Account account);

    List<CartDetail> findByAccount_UserName(String userName);

    @Modifying
    @Query("DELETE FROM CartDetail cd WHERE cd.account.id = :accountId")
    void deleteByAccountId(Integer accountId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartDetail c WHERE c.account = :account")
    void deleteByAccount(@Param("account") Account account);

      List<CartDetail> findByAccountId(Integer accountId);

}
