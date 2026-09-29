package org.example.contentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.contentservice.entity.Post;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
