package org.example.contentservice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@TableName("db_post_tombstone")
@AllArgsConstructor
@NoArgsConstructor
public class PostTombstone {
    @TableId
    private Integer postId;
    private Date deletedAt;
    private Date retainUntil;
}
