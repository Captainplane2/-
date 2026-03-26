package com.esports.zds.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esports.zds.common.Result;
import com.esports.zds.entity.Comment;
import com.esports.zds.entity.Post;
import com.esports.zds.service.CommunityService;

/**
 * 社区模块控制器
 */
@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    /**
     * 发帖
     */
    @PostMapping("/post/publish")
    public Result<Post> publishPost(@RequestBody Post post) {
        Post savedPost = communityService.publishPost(post);
        return Result.success("发布成功", savedPost);
    }

    /**
     * 获取帖子列表
     */
    @GetMapping("/post/list")
    public Result<List<Post>> listPosts(@RequestParam(required = false) String gameProject) {
        return Result.success(communityService.listPosts(gameProject));
    }

    /**
     * 获取我的帖子
     */
    @GetMapping("/post/my/{userId}")
    public Result<List<Post>> listMyPosts(@PathVariable Long userId) {
        return Result.success(communityService.listMyPosts(userId));
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/post/{id}")
    public Result<Post> getPostInfo(@PathVariable Long id) {
        return Result.success(communityService.getPostById(id));
    }

    /**
     * 点赞帖子
     */
    @PostMapping("/post/like/{id}")
    public Result<String> likePost(@PathVariable Long id) {
        communityService.likePost(id);
        return Result.success("点赞成功", null);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/post/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        communityService.deletePost(id);
        return Result.success("删除成功", null);
    }

    /**
     * 发布评论
     */
    @PostMapping("/comment/publish")
    public Result<Comment> publishComment(@RequestBody Comment comment) {
        Comment savedComment = communityService.publishComment(comment);
        return Result.success("评论成功", savedComment);
    }

    /**
     * 获取帖子评论列表
     */
    @GetMapping("/comment/list/{postId}")
    public Result<List<Comment>> listComments(@PathVariable Long postId) {
        return Result.success(communityService.listComments(postId));
    }
}
