package org.example.contentservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.contentservice.entity.Comment;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
