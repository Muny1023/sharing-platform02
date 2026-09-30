package org.example.notificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "content-service")
public interface ContentClient {
    @PostMapping("/internal/content/posts/status")
    Map<Integer, Boolean> postStatus(@RequestBody List<Integer> ids);

    @PostMapping("/internal/content/comments/status")
    Map<Integer, Boolean> commentStatus(@RequestBody List<Integer> ids);
}
