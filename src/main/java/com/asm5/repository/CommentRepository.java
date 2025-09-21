package com.asm5.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asm5.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByNewsId(Long newsId);

    List<Comment> findByNewsIdOrderByCreatedAtAsc(Long newsId);
}
