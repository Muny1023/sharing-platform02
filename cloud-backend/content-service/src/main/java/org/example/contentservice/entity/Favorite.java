package org.example.contentservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@TableName("db_favorite")
@AllArgsConstructor
@NoArgsConstructor
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer postId;
    private Date createTime;
    private Boolean postDeleted;

    public Favorite(Integer id, Integer userId, Integer postId, Date createTime) {
        this(id, userId, postId, createTime, false);
    }
}
