package com.esports.zds.service;

import com.esports.zds.entity.Comment;
import com.esports.zds.entity.Post;
import com.esports.zds.entity.User;
import com.esports.zds.repository.CommentRepository;
import com.esports.zds.repository.PostRepository;
import com.esports.zds.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 发布或更新帖子
     */
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    /**
     * 获取帖子列表 (可按条件)
     */
    public List<Post> listPosts(String category, String gameProject, String keyword, String sortBy, String sortOrder) {
        List<Post> posts;
        
        // 基础查询
        if (category != null && !category.isEmpty() && !"全部".equals(category)) {
            posts = postRepository.findByCategoryOrderByCreateTimeDesc(category);
        } else if (gameProject != null && !gameProject.isEmpty() && !"全部".equals(gameProject)) {
            posts = postRepository.findByGameProjectOrderByCreateTimeDesc(gameProject);
        } else {
            posts = postRepository.findAllByOrderByCreateTimeDesc();
        }
        
        // 搜索过滤
        if (keyword != null && !keyword.isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            posts = posts.stream()
                .filter(post -> post.getTitle().toLowerCase().contains(lowerKeyword) || 
                               post.getContent().toLowerCase().contains(lowerKeyword))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // 排序
        if (sortBy != null && !sortBy.isEmpty()) {
            java.util.Comparator<Post> comparator;
            switch (sortBy) {
                case "views":
                    comparator = java.util.Comparator.comparingInt(Post::getViews);
                    break;
                case "comments":
                    comparator = java.util.Comparator.comparingInt(Post::getComments);
                    break;
                case "createTime":
                default:
                    comparator = java.util.Comparator.comparing(Post::getCreateTime);
                    break;
            }
            
            if ("desc".equals(sortOrder)) {
                comparator = comparator.reversed();
            }
            
            posts.sort(comparator);
        }
        
        // 添加用户头像信息
        for (Post post : posts) {
            if (post.getUserId() != null) {
                User user = userRepository.findById(post.getUserId()).orElse(null);
                if (user != null && user.getAvatar() != null) {
                    post.setAvatar(user.getAvatar());
                }
            }
        }
        
        return posts;
    }

    /**
     * 获取某个用户的帖子
     */
    public java.util.Map<String, Object> listUserPosts(Long userId, Integer status, String sortBy, String sortOrder, Integer page, Integer pageSize) {
        List<Post> posts;
        
        // 基础查询
        if (status != null) {
            posts = postRepository.findByUserIdAndStatusOrderByCreateTimeDesc(userId, status);
        } else {
            posts = postRepository.findByUserIdOrderByCreateTimeDesc(userId);
        }
        
        // 排序
        if (sortBy != null && !sortBy.isEmpty()) {
            java.util.Comparator<Post> comparator;
            switch (sortBy) {
                case "views":
                    comparator = java.util.Comparator.comparingInt(Post::getViews);
                    break;
                case "comments":
                    comparator = java.util.Comparator.comparingInt(Post::getComments);
                    break;
                case "createTime":
                default:
                    comparator = java.util.Comparator.comparing(Post::getCreateTime);
                    break;
            }
            
            if ("desc".equals(sortOrder)) {
                comparator = comparator.reversed();
            }
            
            posts.sort(comparator);
        }
        
        // 分页
        int total = posts.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Post> paginatedPosts = posts.subList(start, end);
        
        // 添加用户头像信息
        for (Post post : paginatedPosts) {
            if (post.getUserId() != null) {
                User user = userRepository.findById(post.getUserId()).orElse(null);
                if (user != null && user.getAvatar() != null) {
                    post.setAvatar(user.getAvatar());
                }
            }
        }
        
        // 构建返回结果
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", paginatedPosts);
        result.put("total", total);
        
        return result;
    }

    /**
     * 发布帖子
     */
    public Post publishPost(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            post.setStatus(0); // 0表示已发布
            postRepository.save(post);
        }
        return post;
    }

    /**
     * 获取帖子详情及增加浏览量
     */
    public Post getPostDetail(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            post.setViews(post.getViews() + 1);
            postRepository.save(post);
            
            // 添加用户头像信息
            if (post.getUserId() != null) {
                User user = userRepository.findById(post.getUserId()).orElse(null);
                if (user != null && user.getAvatar() != null) {
                    post.setAvatar(user.getAvatar());
                }
            }
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
        List<Comment> comments = commentRepository.findByPostIdOrderByCreateTimeAsc(postId);
        
        // 添加用户头像信息
        for (Comment comment : comments) {
            if (comment.getUserId() != null) {
                User user = userRepository.findById(comment.getUserId()).orElse(null);
                if (user != null && user.getAvatar() != null) {
                    comment.setAvatar(user.getAvatar());
                }
            }
        }
        
        return comments;
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
