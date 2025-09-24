package org.jbd.backend.community.repository;

import org.jbd.backend.community.domain.Comment;
import org.jbd.backend.community.domain.Post;
import org.jbd.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostOrderByCreatedAtAsc(Post post);

    List<Comment> findByAuthorOrderByCreatedAtDesc(User author);

    List<Comment> findByPostAndParentCommentIsNullOrderByCreatedAtAsc(Post post);

    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parentComment);

    long countByPost(Post post);

    long countByParentComment(Comment parentComment);
}