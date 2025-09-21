package com.asm5.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "units")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "nvarchar(100)", nullable = false)
    private String name; // Ví dụ: "kg", "lít", "chai", "bao", "gói"

    @Column(name = "width")
    private Double width; // Chiều rộng (đơn vị: cm hoặc m)

    @Column(name = "length")
    private Double length; // Chiều dài (đơn vị: cm hoặc m)

    @Column(name = "weight")
    private Double weight; 

    @Column(name = "height")
    private Double height; // Chiều cao (đơn vị: cm hoặc m)// Cân nặng (đơn vị: kg)
}
