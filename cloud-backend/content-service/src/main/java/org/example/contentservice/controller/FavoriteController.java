package org.example.contentservice.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.cloudcommon.RestBean;
import org.example.contentservice.entity.request.FavoriteToggleRequest;
import org.example.contentservice.entity.response.FavoriteItemResponse;
import org.example.contentservice.entity.response.PageResponse;
import org.example.contentservice.service.FavoriteService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {
    @Resource
    private FavoriteService favoriteService;

    @PostMapping("/toggle")
    public RestBean<Map<String, Object>> toggle(@RequestBody @Valid FavoriteToggleRequest request,
                                                @RequestAttribute("id") Integer userId) {
        FavoriteService.ToggleResult result = favoriteService.toggle(userId, request);
        return result.message() == null
                ? RestBean.success(Map.of("favorited", result.favorited()))
                : RestBean.failure(400, result.message());
    }

    @GetMapping("/status")
    public RestBean<Boolean> status(@RequestParam Integer postId,
                                    @RequestAttribute("id") Integer userId) {
        return RestBean.success(favoriteService.isFavorited(userId, postId));
    }

    @GetMapping("/list")
    public RestBean<PageResponse<FavoriteItemResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestAttribute("id") Integer userId) {
        return RestBean.success(favoriteService.listMine(userId, page, size, sort));
    }
}
