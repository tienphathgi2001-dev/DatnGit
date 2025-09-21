package com.asm5.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import jakarta.persistence.*;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 3, message = "Tên sản phẩm phải có ít nhất 3 ký tự")
    @Column(columnDefinition = "nvarchar(255)")
    private String name;

    private String slug;

    private String image;

    @Column(columnDefinition = "nvarchar(1000)")
    private String description;

    @Min(value = 1, message = "Giá phải lớn hơn 0")
    private int price;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;

    @Temporal(TemporalType.DATE)
    private Date createdDate;

    private Boolean actived;

    @NotNull(message = "Vui lòng chọn loại sản phẩm")
    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonBackReference
    private Category category;

    @NotNull(message = "Vui lòng chọn đơn vị tính")
    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @OneToMany(mappedBy = "product")
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "product")
    private List<CartDetail> cartDetails;


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
private List<Review> reviews;


}
