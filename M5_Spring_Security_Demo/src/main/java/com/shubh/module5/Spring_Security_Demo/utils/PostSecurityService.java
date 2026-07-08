package com.shubh.module5.Spring_Security_Demo.utils;

import com.shubh.module5.Spring_Security_Demo.dto.PostDTO;
import com.shubh.module5.Spring_Security_Demo.entity.UserEntity;
import com.shubh.module5.Spring_Security_Demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostSecurityService {
    private final PostService postService;

    public boolean isOwnerOfPost(Long postId) {

        // Retrieve the currently authenticated user from the SecurityContext.
        // Spring Security maintains a separate SecurityContext for every request
        // (using a ThreadLocal internally), ensuring each request accesses only
        // its own Authentication object and authenticated principal.
        //
        // This method assumes the endpoint is already protected using
        // authenticated() or @PreAuthorize/@Secured. Otherwise, an anonymous
        // request would have the principal "anonymousUser" instead of UserEntity.
        // and will throw a Class Cast Exception
        UserEntity user = (UserEntity) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        // Fetch the post and verify that the authenticated user is its owner.
        PostDTO post = postService.getPostById(postId);

        return user.getId().equals(post.getAuthor().getId());
    }
}
