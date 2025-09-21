package com.asm5.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asm5.model.Account;
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Account findByEmail(String email);

    List<Account> findAllByFullName(String fullName);
    Optional<Account> findByUserName(String userName);

    Page<Account> findByRole(String role, Pageable pageable);

}

