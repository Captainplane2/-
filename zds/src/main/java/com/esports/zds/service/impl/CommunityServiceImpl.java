package com.esports.zds.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esports.zds.entity.Comment;
import com.esports.zds.entity.Post;
import com.esports.zds.repository.CommentRepository;
import com.esports.zds.repository.PostRepository;
import com.esports.zds.service.CommunityService;

/**
 * 社区业务实现类
 */
@Service
public class CommunityServiceImpl implements CommunityService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public Post publishPost(Post post) {
        post.setStatus(0);
        post.setViews(0);
        post.setLikes(0);
        post.setComments(0);
        return postRepository.save(post);
    }

    @Override
    public List<Post> listPosts(String gameProject) {
        if (gameProject != null && !gameProject.isEmpty()) {
            return postRepository.findByGameProjectAndStatusOrderByCreateTimeDesc(gameProject, 0);
        }
        return postRepository.findByStatusOrderByCreateTimeDesc(0);
    }

    @Override
    public List<Post> listMyPosts(Long userId) {
        return postRepository.findByUserIdAndStatusOrderByCreateTimeDesc(userId, 0);
    }

    @Override
    public Post getPostById(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null && post.getStatus() == 0) {
            post.setViews(post.getViews() + 1);
            postRepository.save(post);
        }
        return post;
    }

    @Override
    public void likePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow();
        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);
    }

    @Override
    public void deletePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow();
        post.setStatus(1); // 逻辑删除
        postRepository.save(post);
    }

    @Override
    @Transactional
    public Comment publishComment(Comment comment) {
        Comment savedComment = commentRepository.save(comment);
        
        // 更新帖子评论数
        Post post = postRepository.findById(comment.getPostId()).orElseThrow();
        post.setComments(post.getComments() + 1);
        postRepository.save(post);
        
        return savedComment;
    }

    @Override
    public List<Comment> listComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreateTimeAsc(postId);
    }
}
