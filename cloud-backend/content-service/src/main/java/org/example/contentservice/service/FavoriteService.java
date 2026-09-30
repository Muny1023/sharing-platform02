package org.example.contentservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.contentservice.entity.Favorite;
import org.example.contentservice.entity.request.FavoriteToggleRequest;
import org.example.contentservice.entity.response.FavoriteItemResponse;
import org.example.contentservice.entity.response.PageResponse;

public interface FavoriteService extends IService<Favorite> {
    ToggleResult toggle(Integer userId, FavoriteToggleRequest request);

    boolean isFavorited(Integer userId, Integer postId);

    PageResponse<FavoriteItemResponse> listMine(Integer userId, int page, int size, String sort);

    record ToggleResult(boolean favorited, String message) {
    }
}
