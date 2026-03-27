package com.esports.zds.controller;

import com.esports.zds.common.Result;
import com.esports.zds.entity.Comment;
import com.esports.zds.entity.Post;
import com.esports.zds.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社区论坛控制器
 */
@RestController
@RequestMapping("/api/post")
public class PostController {

    @Autowired
    private PostService postService;

    /**
     * 发布或更新帖子
     */
    @PostMapping("/save")
    public Result<Post> savePost(@RequestBody Post post) {
        return Result.success("帖子发布成功", postService.savePost(post));
    }

    /**
     * 获取帖子列表
     */
    @GetMapping("/list")
    public Result<List<Post>> listPosts(@RequestParam(required = false) String category,
                                      @RequestParam(required = false) String gameProject) {
        return Result.success(postService.listPosts(category, gameProject));
    }

    /**
     * 获取我的帖子
     */
    @GetMapping("/my/{userId}")
    public Result<List<Post>> listMyPosts(@PathVariable Long userId) {
        return Result.success(postService.listUserPosts(userId));
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{id}")
    public Result<Post> getPostDetail(@PathVariable Long id) {
        Post post = postService.getPostDetail(id);
        if (post != null) {
            return Result.success(post);
        }
        return Result.error(404, "帖子已不存在");
    }

    /**
     * 发布评论
     */
    @PostMapping("/comment/add")
    public Result<Comment> addComment(@RequestBody Comment comment) {
        return Result.success("评论成功", postService.addComment(comment));
    }

    /**
     * 获取帖子评论列表
     */
    @GetMapping("/comment/list/{postId}")
    public Result<List<Comment>> listComments(@PathVariable Long postId) {
        return Result.success(postService.listComments(postId));
    }

    /**
     * 删除帖子 (管理端或作者本人)
     */
    @PostMapping("/delete/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return Result.success("帖子删除成功", null);
    }
}
