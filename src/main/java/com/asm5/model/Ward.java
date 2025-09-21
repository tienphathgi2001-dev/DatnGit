package com.asm5.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma")
    private String ma;

    @Column(name = "ten", columnDefinition = "nvarchar(255)")
    private String ten;

    @ManyToOne
    @JoinColumn(name = "district_id")
    @JsonIgnore
    @ToString.Exclude
    private District district;
}
