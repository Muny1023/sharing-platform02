package org.example.notificationservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.notificationservice.entity.Notification;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
