package org.example.contentservice.controller;

import jakarta.annotation.Resource;
import org.example.contentservice.entity.Comment;
import org.example.contentservice.entity.Post;
import org.example.contentservice.mapper.CommentMapper;
import org.example.contentservice.mapper.PostMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/content")
public class InternalContentController {
    @Resource
    private PostMapper postMapper;

    @Resource
    private CommentMapper commentMapper;

    @PostMapping("/posts/status")
    public Map<Integer, Boolean> postStatus(@RequestBody List<Integer> ids) {
        return status(ids, postMapper.selectBatchIds(ids).stream().map(Post::getId).toList());
    }

    @PostMapping("/comments/status")
    public Map<Integer, Boolean> commentStatus(@RequestBody List<Integer> ids) {
        return status(ids, commentMapper.selectBatchIds(ids).stream().map(Comment::getId).toList());
    }

    private Map<Integer, Boolean> status(List<Integer> requested, List<Integer> existing) {
        var found = existing.stream().collect(Collectors.toSet());
        return requested.stream().distinct().collect(Collectors.toMap(Function.identity(), found::contains));
    }
}
