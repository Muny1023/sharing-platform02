package org.example.contentservice.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.cloudcommon.RestBean;
import org.example.contentservice.entity.request.CommentCreateRequest;
import org.example.contentservice.entity.response.CommentResponse;
import org.example.contentservice.service.CommentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/comment")
public class CommentController {
    @Resource
    private CommentService commentService;

    @PostMapping
    public RestBean<Void> create(@RequestBody @Valid CommentCreateRequest request,
                                 @RequestAttribute("id") Integer authorId) {
        return message(() -> commentService.createComment(authorId, request));
    }

    @GetMapping("/list/{postId}")
    public RestBean<List<CommentResponse>> list(@PathVariable Integer postId) {
        return RestBean.success(commentService.listCommentsByPost(postId));
    }

    private RestBean<Void> message(Supplier<String> operation) {
        String message = operation.get();
        return message == null ? RestBean.success() : RestBean.failure(400, message);
    }
}
