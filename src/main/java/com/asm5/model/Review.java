package com.asm5.model;

import java.time.LocalDateTime;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private int rating;
   
   @Column(nullable = false, columnDefinition = "nvarchar(500)") // cho phép comment dài và lưu tiếng Việt
    private String comment;

    @CreationTimestamp
@Column(name = "created_at", updatable = false)
private LocalDateTime createdAt;

    // getter setter
}

