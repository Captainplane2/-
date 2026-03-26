package com.esports.zds.service;

import java.util.List;

import com.esports.zds.entity.Comment;
import com.esports.zds.entity.Post;

/**
 * 社区业务接口
 */
public interface CommunityService {
    
    // 帖子相关
    Post publishPost(Post post);
    List<Post> listPosts(String gameProject);
    List<Post> listMyPosts(Long userId);
    Post getPostById(Long id);
    void likePost(Long id);
    void deletePost(Long id);

    // 评论相关
    Comment publishComment(Comment comment);
    List<Comment> listComments(Long postId);
}
