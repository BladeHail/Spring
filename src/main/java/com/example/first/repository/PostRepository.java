package com.example.first.repository;

import com.example.first.dto.response.PostListDto;
import com.example.first.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
        select new com.example.first.dto.response.PostListDto(
            p.id,
            p.title,
            u.username,
            p.createdAt
        )
        from Post p
        join User u on p.authorId = u.id
        where p.deleted = false
        order by p.createdAt desc
    """)
    Page<PostListDto> findPostList(Pageable pageable);
}

