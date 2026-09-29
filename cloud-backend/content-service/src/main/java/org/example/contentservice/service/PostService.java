package org.example.contentservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.contentservice.entity.Post;
import org.example.contentservice.entity.request.PostCreateRequest;
import org.example.contentservice.entity.request.PostUpdateRequest;
import org.example.contentservice.entity.response.PageResponse;
import org.example.contentservice.entity.response.PostDetailResponse;
import org.example.contentservice.entity.response.PostListResponse;

public interface PostService extends IService<Post> {
    String createPost(Integer authorId, PostCreateRequest request);
    String updatePost(Integer authorId, Integer postId, PostUpdateRequest request);
    String deletePost(Integer authorId, Integer postId);
    PageResponse<PostListResponse> listPosts(int page, int size, String sort);
    PageResponse<PostListResponse> listMyPosts(Integer authorId, int page, int size, String sort);
    PageResponse<PostListResponse> searchPosts(String keyword, int page, int size);
    PostDetailResponse getPostDetail(Integer id);
}
