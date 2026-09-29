package org.example.contentservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@TableName("db_post")
@AllArgsConstructor
@NoArgsConstructor
public class Post {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer authorId;
    private String title;
    private String content;
    private String resourceUrl;
    private Integer likeCount;
    private Integer commentCount;
    private Date createTime;
    private Date updateTime;
    private Boolean deleted;
    private Integer favoriteCount;

    public Post(Integer id, Integer authorId, String title, String content, String resourceUrl,
                Integer likeCount, Integer commentCount, Date createTime, Date updateTime) {
        this(id, authorId, title, content, resourceUrl, likeCount, commentCount,
                createTime, updateTime, false, 0);
    }
}
