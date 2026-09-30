package org.example.notificationservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudcommon.AccountSummary;
import org.example.cloudcommon.NotificationEvent;
import org.example.notificationservice.client.AccountClient;
import org.example.notificationservice.client.ContentClient;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.entity.response.NotificationPageResponse;
import org.example.notificationservice.entity.response.NotificationResponse;
import org.example.notificationservice.mapper.NotificationMapper;
import org.example.notificationservice.service.NotificationService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {
    private static final String REDIS_PREFIX = "notification:unread:";

    @Resource
    private StringRedisTemplate redis;

    @Resource
    private AccountClient accountClient;

    @Resource
    private ContentClient contentClient;

    @Override
    public long unreadCount(Integer recipientId) {
        String key = REDIS_PREFIX + recipientId;
        String cached = redis.opsForValue().get(key);
        if (cached != null) return parse(cached);
        long count = count(Wrappers.<Notification>query().eq("recipient_id", recipientId).eq("is_read", false));
        redis.opsForValue().set(key, String.valueOf(count));
        return count;
    }

    @Override
    public NotificationPageResponse page(Integer recipientId, int page, int size, String filter) {
        Page<Notification> result = new Page<>(Math.max(1, page), Math.min(Math.max(1, size), 100));
        var query = Wrappers.<Notification>query().eq("recipient_id", recipientId).orderByDesc("created_at");
        if ("unread".equalsIgnoreCase(filter)) query.eq("is_read", false);
        else if ("read".equalsIgnoreCase(filter)) query.eq("is_read", true);
        page(result, query);

        Map<Integer, String> names = loadNicknames(result.getRecords());
        Map<Integer, Boolean> postStatus = loadPostStatus(result.getRecords());
        Map<Integer, Boolean> commentStatus = loadCommentStatus(result.getRecords());
        List<NotificationResponse> items = result.getRecords().stream().map(notification -> {
            boolean postDeleted = notification.getPostId() != null
                    && postStatus.containsKey(notification.getPostId())
                    && !Boolean.TRUE.equals(postStatus.get(notification.getPostId()));
            boolean commentDeleted = notification.getCommentId() != null
                    && commentStatus.containsKey(notification.getCommentId())
                    && !Boolean.TRUE.equals(commentStatus.get(notification.getCommentId()));
            return new NotificationResponse(notification.getId(), notification.getActorId(),
                    names.getOrDefault(notification.getActorId(), "未知用户"), notification.getType(),
                    notification.getPostId(), notification.getCommentId(), notification.getContent(),
                    notification.getCreatedAt(), Boolean.TRUE.equals(notification.getIsRead()),
                    postDeleted, commentDeleted);
        }).toList();
        return new NotificationPageResponse(result.getCurrent(), result.getSize(), result.getTotal(),
                unreadCount(recipientId), items);
    }

    @Override
    @Transactional
    public String markRead(Integer recipientId, Integer notificationId) {
        Notification notification = getOne(Wrappers.<Notification>query().eq("id", notificationId)
                .eq("recipient_id", recipientId));
        if (notification == null) return "通知不存在";
        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            update().eq("id", notificationId).set("is_read", true).update();
            decrement(recipientId, 1);
        }
        return null;
    }

    @Override
    @Transactional
    public long markAllRead(Integer recipientId) {
        long count = count(Wrappers.<Notification>query().eq("recipient_id", recipientId).eq("is_read", false));
        if (count > 0) update().eq("recipient_id", recipientId).eq("is_read", false)
                .set("is_read", true).update();
        redis.opsForValue().set(REDIS_PREFIX + recipientId, "0");
        return count;
    }

    @Override
    @Transactional
    public String deleteOne(Integer recipientId, Integer notificationId) {
        Notification notification = getOne(Wrappers.<Notification>query().eq("id", notificationId)
                .eq("recipient_id", recipientId));
        if (notification == null) return "通知不存在";
        if (!removeById(notificationId)) return "内部错误，请联系管理员";
        if (!Boolean.TRUE.equals(notification.getIsRead())) decrement(recipientId, 1);
        return null;
    }

    @Override
    @Transactional
    public long deleteRead(Integer recipientId) {
        long count = count(Wrappers.<Notification>query().eq("recipient_id", recipientId).eq("is_read", true));
        if (count > 0) remove(Wrappers.<Notification>query().eq("recipient_id", recipientId).eq("is_read", true));
        return count;
    }

    @Override
    @Transactional
    public void consume(NotificationEvent event) {
        if (event == null || event.eventId() == null || event.recipientId() == null
                || event.actorId() == null || event.recipientId().equals(event.actorId())) return;
        if (getOne(Wrappers.<Notification>query().eq("event_id", event.eventId())) != null) return;
        try {
            if (!save(new Notification(null, event.recipientId(), event.actorId(), event.type(), event.postId(),
                    event.commentId(), event.content(), new Date(), false, event.eventId()))) return;
        } catch (DuplicateKeyException duplicate) {
            log.debug("notification event {} was already consumed", event.eventId());
            return;
        }
        redis.opsForValue().increment(REDIS_PREFIX + event.recipientId());
    }

    @Override
    @Scheduled(cron = "0 0 3 * * *")
    public long cleanupExpired() {
        Date cutoff = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90));
        return baseMapper.delete(Wrappers.<Notification>query().lt("created_at", cutoff));
    }

    @Override
    public List<Integer> listUnreadCommentIds(Integer recipientId, Integer postId) {
        return list(Wrappers.<Notification>query().eq("recipient_id", recipientId).eq("post_id", postId)
                .eq("is_read", false).in("type", List.of("POST_COMMENT", "COMMENT_REPLY"))).stream()
                .map(Notification::getCommentId).filter(Objects::nonNull).distinct().toList();
    }

    private Map<Integer, String> loadNicknames(List<Notification> notifications) {
        List<Integer> ids = notifications.stream().map(Notification::getActorId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return Map.of();
        try {
            return accountClient.findByIds(ids).stream().collect(Collectors.toMap(
                    AccountSummary::id, AccountSummary::nickname, (first, ignored) -> first));
        } catch (RuntimeException ignored) {
            return Map.of();
        }
    }

    private Map<Integer, Boolean> loadPostStatus(List<Notification> notifications) {
        List<Integer> ids = notifications.stream().map(Notification::getPostId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return Map.of();
        try {
            return contentClient.postStatus(ids);
        } catch (RuntimeException ignored) {
            return Map.of();
        }
    }

    private Map<Integer, Boolean> loadCommentStatus(List<Notification> notifications) {
        List<Integer> ids = notifications.stream().map(Notification::getCommentId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return Map.of();
        try {
            return contentClient.commentStatus(ids);
        } catch (RuntimeException ignored) {
            return Map.of();
        }
    }

    private void decrement(Integer recipientId, long delta) {
        String key = REDIS_PREFIX + recipientId;
        redis.opsForValue().decrement(key, delta);
        String value = redis.opsForValue().get(key);
        if (value != null && parse(value) < 0) redis.opsForValue().set(key, "0");
    }

    private long parse(String value) {
        try {
            return Math.max(0, Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
