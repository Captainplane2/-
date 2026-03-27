package com.esports.zds.service;

import com.esports.zds.entity.Comment;
import com.esports.zds.entity.Post;
import com.esports.zds.repository.CommentRepository;
import com.esports.zds.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CommentRepository commentRepository;

    /**
     * 发布或更新帖子
     */
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    /**
     * 获取帖子列表 (可按条件)
     */
    public List<Post> listPosts(String category, String gameProject) {
        if (category != null && !category.isEmpty() && !"全部".equals(category)) {
            return postRepository.findByCategoryOrderByCreateTimeDesc(category);
        } else if (gameProject != null && !gameProject.isEmpty() && !"全部".equals(gameProject)) {
            return postRepository.findByGameProjectOrderByCreateTimeDesc(gameProject);
        }
        return postRepository.findAllByOrderByCreateTimeDesc();
    }

    /**
     * 获取某个用户的帖子
     */
    public List<Post> listUserPosts(Long userId) {
        return postRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    /**
     * 获取帖子详情及增加浏览量
     */
    public Post getPostDetail(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            post.setViews(post.getViews() + 1);
            postRepository.save(post);
        }
        return post;
    }

    /**
     * 添加评论
     */
    public Comment addComment(Comment comment) {
        Comment savedComment = commentRepository.save(comment);
        // 更新帖子评论数
        Post post = postRepository.findById(comment.getPostId()).orElse(null);
        if (post != null) {
            post.setComments(post.getComments() + 1);
            postRepository.save(post);
        }
        return savedComment;
    }

    /**
     * 获取帖子评论列表
     */
    public List<Comment> listComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreateTimeAsc(postId);
    }

    /**
     * 删除帖子 (连同评论)
     */
    public void deletePost(Long id) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreateTimeAsc(id);
        commentRepository.deleteAll(comments);
        postRepository.deleteById(id);
    }
}
