package com.asm5.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "Address")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;


    @ManyToOne
@JoinColumn(name = "account_id")
@ToString.Exclude
private Account account;

    

    @ManyToOne
    @JoinColumn(name = "ward_id") // đổi từ "xa_id"
    private Ward ward;


    @Column(columnDefinition = "nvarchar(200)")
    private String houseNumber; // <i> từ "soNha"

    @Column(columnDefinition = "bit default 0")
    private Boolean isDefault;



}
