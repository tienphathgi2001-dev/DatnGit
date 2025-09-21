package com.asm5.repository;

import com.asm5.model.Account;
import com.asm5.model.Address;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {

    // Sử dụng @EntityGraph để tránh LazyInitializationException, tự load ward -> district -> province
    @EntityGraph(attributePaths = {
        "ward", "ward.district", "ward.district.province"
    })
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.account.id = :accountId AND a.id <> :addressId")
    void unsetDefaultForOtherAddresses(@Param("accountId") Integer accountId, @Param("addressId") Integer addressId);

    Optional<Address> findByAccountAndIsDefaultTrue(Account account);


    List<Address> findByAccount(Account account);

    // Tùy chọn lấy theo accountId (nếu cần)
    List<Address> findByAccountId(Integer accountId);

    // Query lấy danh sách địa chỉ với full join để chắc chắn dữ liệu đầy đủ
    @Query("SELECT a FROM Address a " +
           "JOIN FETCH a.ward w " +
           "JOIN FETCH w.district d " +
           "JOIN FETCH d.province p " +
           "WHERE a.account = :account")
    List<Address> findByAccountWithFullAddress(@Param("account") Account account);

    // Query lấy địa chỉ mặc định của tài khoản
    @Query("SELECT a FROM Address a " +
           "JOIN FETCH a.ward w " +
           "JOIN FETCH w.district d " +
           "JOIN FETCH d.province p " +
           "WHERE a.account.id = :accountId AND a.isDefault = true")
    Optional<Address> findDefaultByAccountId(@Param("accountId") Integer accountId);
}
