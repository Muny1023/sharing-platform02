package org.example.contentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.contentservice.entity.Favorite;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    @Select("SELECT f.* FROM db_favorite f LEFT JOIN db_post p ON p.id = f.post_id "
            + "WHERE f.user_id = #{userId} ORDER BY COALESCE(p.like_count, 0) DESC, f.create_time DESC")
    Page<Favorite> selectByHot(Page<Favorite> page, @Param("userId") Integer userId);

    @Update("UPDATE db_favorite SET post_deleted = 1 WHERE post_id = #{postId}")
    int markPostDeleted(@Param("postId") Integer postId);
}
