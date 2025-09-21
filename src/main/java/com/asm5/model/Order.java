package com.asm5.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    private int total;
    private int feeship;
    private int discount;

    private int status;

    private Boolean paymentStatus;

    private int paymentMethod;

    @Column(columnDefinition = "nvarchar(255)")
    private String address;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @ManyToOne
    @JoinColumn(name = "discount_code_id")
    private DiscountCode discountCode;
}
