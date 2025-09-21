package com.asm5.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "discount_codes")
public class DiscountCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String code;

    private String description;

    private Boolean active;

    private Double discountAmount;   // số tiền giảm trực tiếp
    private Integer discountPercent; // % giảm

    private Long expirationDate; // lưu epoch milli

    private Integer maxUses;    // số lần sử dụng tối đa
    private Integer usedCount;  // đã sử dụng bao nhiêu lần
}
