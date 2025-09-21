package com.asm5.model;

import java.time.LocalDate;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // thêm tự tăng nếu cần
    private Integer id;

    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

@Column(name = "full_name", columnDefinition = "nvarchar(250)")
    private String fullName;
    
    private String avatar;
    
    @Column(nullable = false)
    private Boolean activated = true;


    // ✅ Thêm vai trò (role)
    @Column(nullable = false)
    private String role = "USER"; // mặc định là USER
    
    @Column(nullable = false)
    private Boolean admin = false;

    
    @Column(name = "created_date")
    private LocalDate createdDate;

   @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
@ToString.Exclude
private List<Address> addresses;

@OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
@ToString.Exclude
private List<Order> orders;

@OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
@ToString.Exclude
private List<CartDetail> cartDetails;

@OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
private List<Review> reviews;


}
