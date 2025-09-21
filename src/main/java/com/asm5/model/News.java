package com.asm5.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "news")
public class News {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "nvarchar(500)")
    private String title;

    @Column(nullable = false, columnDefinition = "ntext")
    private String content;

    private String image; // ảnh minh hoạ

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Người đăng tin (Admin)
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account author; 

    // Bình luận của user
    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;
}
