package org.example.contentservice.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.cloudcommon.RestBean;
import org.example.contentservice.entity.request.PostCreateRequest;
import org.example.contentservice.entity.request.PostUpdateRequest;
import org.example.contentservice.entity.response.PageResponse;
import org.example.contentservice.entity.response.PostDetailResponse;
import org.example.contentservice.entity.response.PostListResponse;
import org.example.contentservice.mapper.PostTombstoneMapper;
import org.example.contentservice.service.PostService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/post")
public class PostController {
    @Resource
    private PostService postService;

    @Resource
    private PostTombstoneMapper tombstoneMapper;

    @PostMapping
    public RestBean<Void> create(@RequestBody @Valid PostCreateRequest request,
                                 @RequestAttribute("id") Integer authorId) {
        return message(postService.createPost(authorId, request));
    }

    @PutMapping("/{id}")
    public RestBean<Void> update(@PathVariable Integer id, @RequestBody @Valid PostUpdateRequest request,
                                 @RequestAttribute("id") Integer authorId) {
        return message(postService.updatePost(authorId, id, request));
    }

    @DeleteMapping("/{id}")
    public RestBean<Void> delete(@PathVariable Integer id, @RequestAttribute("id") Integer authorId) {
        return message(postService.deletePost(authorId, id));
    }

    @GetMapping("/list")
    public RestBean<PageResponse<PostListResponse>> list(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(defaultValue = "latest") String sort) {
        return RestBean.success(postService.listPosts(page, size, sort));
    }

    @GetMapping("/mine")
    public RestBean<PageResponse<PostListResponse>> mine(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(defaultValue = "latest") String sort,
                                                         @RequestAttribute("id") Integer authorId) {
        return RestBean.success(postService.listMyPosts(authorId, page, size, sort));
    }

    @GetMapping("/search")
    public RestBean<PageResponse<PostListResponse>> search(@RequestParam String keyword,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return RestBean.success(postService.searchPosts(keyword, page, size));
    }

    @GetMapping("/{id}")
    public RestBean<PostDetailResponse> detail(@PathVariable Integer id) {
        PostDetailResponse result = postService.getPostDetail(id);
        if (result == null && tombstoneMapper.selectById(id) != null) return RestBean.failure(410, "该帖子已被作者删除");
        if (result != null && result.deleted()) return RestBean.failure(410, "该帖子已被作者删除");
        return result == null ? RestBean.failure(404, "帖子不存在或已被删除") : RestBean.success(result);
    }

    private RestBean<Void> message(String message) {
        return message == null ? RestBean.success() : RestBean.failure(400, message);
    }
}
