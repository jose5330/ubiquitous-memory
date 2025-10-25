package com.example.backend.repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.util.List;
import com.example.backend.model.Post;

@Repository
public interface PostRepo extends JpaRepository<Post, Integer> {
    List<Post> findByUserId(Integer userId);
    Page<Post> findAllByParentId(Integer parentId, Pageable pageable);
    List<Post> findAll();
}
