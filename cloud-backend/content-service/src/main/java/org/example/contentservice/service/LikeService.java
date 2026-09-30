package org.example.contentservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.contentservice.entity.Like;
import org.example.contentservice.entity.request.LikeToggleRequest;

import java.util.List;
import java.util.Set;

public interface LikeService extends IService<Like> {
    LikeToggleResult toggleLike(Integer userId, LikeToggleRequest request);

    Set<Integer> findLikedTargetIds(Integer userId, String targetType, List<Integer> targetIds);

    record LikeToggleResult(String message, boolean liked, int likeCount) {
        public static LikeToggleResult success(boolean liked, int likeCount) {
            return new LikeToggleResult(null, liked, likeCount);
        }

        public static LikeToggleResult failure(String message) {
            return new LikeToggleResult(message, false, 0);
        }
    }
}
