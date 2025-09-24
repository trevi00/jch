package org.jbd.backend.community.service;

import org.jbd.backend.community.dto.CommentDto;

import java.util.List;

public interface CommentService {
    
    CommentDto.Response createComment(CommentDto.CreateRequest request, String authorEmail);
    
    CommentDto.Response getCommentById(Long id);
    
    List<CommentDto.Response> getCommentsByPost(Long postId);
    
    List<CommentDto.Response> getCommentsByAuthor(String authorEmail);
    
    CommentDto.Response updateComment(Long id, CommentDto.UpdateRequest request, String authorEmail);
    
    void deleteComment(Long id, String authorEmail);
    
    List<CommentDto.Response> getRepliesByParentComment(Long parentCommentId);
    
    long getCommentCountByPost(Long postId);
}