package org.example.contentservice.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.cloudcommon.RestBean;
import org.example.contentservice.entity.request.LikeToggleRequest;
import org.example.contentservice.service.LikeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/api/like")
public class LikeController {
    @Resource
    private LikeService likeService;

    @PostMapping("/toggle")
    public RestBean<Map<String, Object>> toggle(@RequestBody @Valid LikeToggleRequest request,
                                                @RequestAttribute("id") Integer userId) {
        LikeService.LikeToggleResult result = likeService.toggleLike(userId, request);
        if (result.message() != null) return RestBean.failure(400, result.message());
        return RestBean.success(Map.of("liked", result.liked()));
    }

    @GetMapping("/status")
    public RestBean<Set<Integer>> status(@RequestParam String targetType,
                                         @RequestParam List<Integer> targetIds,
                                         @RequestAttribute("id") Integer userId) {
        if (!"post".equals(targetType) && !"comment".equals(targetType)) {
            return RestBean.failure(400, "目标类型只能是 post 或 comment");
        }
        return RestBean.success(likeService.findLikedTargetIds(userId, targetType, targetIds));
    }
}
